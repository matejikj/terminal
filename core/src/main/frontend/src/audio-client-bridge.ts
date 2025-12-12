class AudioClientBridgeElement extends HTMLElement {
  private mediaStream?: MediaStream;
  private selectedInput?: string;
  private selectedOutput?: string;
  private audioContext?: AudioContext;
  private mediaRecorder?: MediaRecorder;
  private recordedChunks: BlobPart[] = [];
  private micTestAudio?: HTMLAudioElement;
  private micTestBlobUrl?: string;
  private outputMuted = false;
  private microphoneMuted = false;
  private outputVolume = 1;

  connectedCallback() {
    navigator.mediaDevices?.addEventListener('devicechange', () => this.requestDeviceList());
    this.requestDeviceList();
  }

  disconnectedCallback() {
    this.mediaStream?.getTracks().forEach((track) => track.stop());
    this.mediaStream = undefined;
  }

  private async ensureStream(): Promise<void> {
    if (this.mediaStream) {
      return;
    }
    try {
      this.mediaStream = await navigator.mediaDevices.getUserMedia({
        audio: this.selectedInput
          ? { deviceId: { exact: this.selectedInput } }
          : true,
      });
    } catch (error) {
      this.reportError(error);
    }
  }

  async requestDeviceList() {
    if (!navigator.mediaDevices?.enumerateDevices) {
      this.reportError('Media devices API not available in this browser.');
      return;
    }
    try {
      await this.ensureStream();
      const devices = await navigator.mediaDevices.enumerateDevices();
      const inputs = devices
        .filter((device) => device.kind === 'audioinput')
        .map((device) => ({
          id: device.deviceId,
          label: device.label || 'Microphone',
          type: 'INPUT',
        }));
      const outputs = devices
        .filter((device) => device.kind === 'audiooutput')
        .map((device) => ({
          id: device.deviceId,
          label: device.label || 'Speaker',
          type: 'OUTPUT',
        }));
      // @ts-ignore server bridge provided by Vaadin
      this.$server?.reportDevices(inputs, outputs);
    } catch (error) {
      this.reportError(error);
    }
  }

  async selectDevice(type: string, deviceId: string) {
    if (type === 'INPUT') {
      this.selectedInput = deviceId;
      this.mediaStream?.getTracks().forEach((track) => track.stop());
      this.mediaStream = undefined;
      await this.ensureStream();
    } else {
      this.selectedOutput = deviceId;
    }
    // @ts-ignore
    this.$server?.reportDeviceSelection(type, deviceId);
  }

  setRoute(route: string) {
    // Browsers expose limited control over output routing; notify the server.
    // @ts-ignore
    this.$server?.reportRoute(route);
  }

  setMute(muted: boolean) {
    this.outputMuted = muted;
    if (muted) {
      this.stopMicTestPlayback();
    }
    // @ts-ignore
    this.$server?.reportMute(muted);
  }

  setMicrophoneMute(muted: boolean) {
    this.microphoneMuted = muted;
    this.mediaStream?.getAudioTracks().forEach((track) => {
      track.enabled = !muted;
    });
    if (muted && this.mediaRecorder && this.mediaRecorder.state === 'recording') {
      this.mediaRecorder.stop();
    }
    // @ts-ignore
    this.$server?.reportMicrophoneMute(muted);
  }

  setOutputVolume(volume: number) {
    this.outputVolume = Math.max(0, Math.min(1, volume));
    if (this.micTestAudio) {
      this.micTestAudio.volume = this.outputVolume;
    }
  }

  async playTestSound(deviceId?: string | null) {
    if (this.outputMuted) {
      return;
    }
    try {
      const context = await this.getOrCreateAudioContext();
      const oscillator = context.createOscillator();
      const gain = context.createGain();
      oscillator.type = 'sine';
      oscillator.frequency.value = 880;
      const amplitude = 0.25 * this.outputVolume;
      gain.gain.setValueAtTime(0, context.currentTime);
      gain.gain.linearRampToValueAtTime(amplitude, context.currentTime + 0.02);
      gain.gain.linearRampToValueAtTime(0, context.currentTime + 1);
      oscillator.connect(gain);
      const resolvedTarget = deviceId ?? this.selectedOutput;
      const routed =
        resolvedTarget && this.canSelectOutputDevice()
          ? await this.playThroughDevice(context, gain, oscillator, resolvedTarget)
          : false;
      if (!routed) {
        gain.connect(context.destination);
        oscillator.start();
        oscillator.stop(context.currentTime + 1);
      }
    } catch (error) {
      this.reportError(error);
    }
  }

  private canSelectOutputDevice(): boolean {
    return (
      typeof (HTMLMediaElement.prototype as { setSinkId?: unknown }).setSinkId === 'function'
    );
  }

  async startMicTest() {
    try {
      if (this.microphoneMuted) {
        throw new Error('Microphone is muted');
      }
      if (typeof MediaRecorder === 'undefined') {
        throw new Error('MediaRecorder API not available in this browser.');
      }
      await this.ensureStream();
      if (!this.mediaStream) {
        throw new Error('Microphone stream unavailable');
      }
      if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
        return;
      }
      this.stopMicTestPlayback();
      this.recordedChunks = [];
      const options = this.resolveRecorderMimeType();
      const recorder = options
        ? new MediaRecorder(this.mediaStream, { mimeType: options })
        : new MediaRecorder(this.mediaStream);
      recorder.ondataavailable = (event) => {
        if (event.data && event.data.size > 0) {
          this.recordedChunks.push(event.data);
        }
      };
      recorder.onerror = (event) => {
        this.reportError(event.error || 'Microphone recording error');
      };
      recorder.onstop = async () => {
        try {
          await this.playRecordedAudio();
        } catch (error) {
          this.reportError(error);
        } finally {
          // @ts-ignore
          this.$server?.reportMicTestState(false);
          this.mediaRecorder = undefined;
        }
      };
      recorder.start();
      this.mediaRecorder = recorder;
      // @ts-ignore
      this.$server?.reportMicTestState(true);
    } catch (error) {
      this.reportError(error);
      // @ts-ignore
      this.$server?.reportMicTestState(false);
    }
  }

  stopMicTest() {
    if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
      this.mediaRecorder.stop();
      // @ts-ignore
      this.$server?.reportMicTestState(false);
    } else {
      this.stopMicTestPlayback();
      // @ts-ignore
      this.$server?.reportMicTestState(false);
    }
  }

  private async playThroughDevice(
    context: AudioContext,
    gain: GainNode,
    oscillator: OscillatorNode,
    deviceId: string,
  ): Promise<boolean> {
    let sink: HTMLAudioElement | undefined;
    try {
      const destination = context.createMediaStreamDestination();
      gain.connect(destination);
      sink = new Audio();
      sink.srcObject = destination.stream;
      sink.autoplay = true;
      // @ts-ignore experimental API
      await sink.setSinkId(deviceId);
      oscillator.start();
      await sink.play();
      oscillator.stop(context.currentTime + 1);
      const playbackSink = sink;
      window.setTimeout(() => {
        if (!playbackSink) {
          return;
        }
        gain.disconnect(destination);
        playbackSink.pause();
        playbackSink.srcObject = null;
        playbackSink.remove();
      }, 1200);
      return true;
    } catch (error) {
      try {
        gain.disconnect();
      } catch (_ignored) {
        // ignore disconnect errors
      }
      try {
        oscillator.stop();
      } catch (_ignored) {
        // oscillator might not have been started yet
      }
      if (sink) {
        sink.pause();
        sink.srcObject = null;
        sink.remove();
      }
      this.reportError(error);
      return false;
    }
  }

  private async playRecordedAudio() {
    if (this.recordedChunks.length === 0) {
      return;
    }
    if (this.outputMuted) {
      this.stopMicTestPlayback();
      return;
    }
    const blob = new Blob(this.recordedChunks, {
      type: this.mediaRecorder?.mimeType || 'audio/webm',
    });
    this.stopMicTestPlayback();
    const url = URL.createObjectURL(blob);
    this.micTestBlobUrl = url;
    const audio = new Audio(url);
    this.micTestAudio = audio;
    audio.autoplay = true;
    audio.volume = this.outputVolume;
    const target = this.selectedOutput;
    if (target && this.canSelectOutputDevice()) {
      const sinkId = target;
      // @ts-ignore experimental API
      if (typeof audio.setSinkId === 'function') {
        // @ts-ignore
        await audio.setSinkId(sinkId);
      }
    }
    await audio.play();
    audio.onended = () => this.stopMicTestPlayback();
  }

  private stopMicTestPlayback() {
    if (this.micTestAudio) {
      this.micTestAudio.pause();
      this.micTestAudio.src = '';
      this.micTestAudio.load();
      this.micTestAudio = undefined;
    }
    if (this.micTestBlobUrl) {
      URL.revokeObjectURL(this.micTestBlobUrl);
      this.micTestBlobUrl = undefined;
    }
    this.recordedChunks = [];
  }

  private resolveRecorderMimeType(): string | undefined {
    const candidates = [
      'audio/webm;codecs=opus',
      'audio/webm',
      'audio/ogg;codecs=opus',
      'audio/ogg',
    ];
    if (typeof MediaRecorder === 'undefined') {
      return undefined;
    }
    return candidates.find((type) => MediaRecorder.isTypeSupported(type));
  }

  private async getOrCreateAudioContext(): Promise<AudioContext> {
    if (!this.audioContext) {
      this.audioContext = new AudioContext();
    }
    if (this.audioContext.state === 'suspended') {
      await this.audioContext.resume();
    }
    return this.audioContext;
  }

  private reportError(error: unknown) {
    const message =
      typeof error === 'string'
        ? error
        : error instanceof Error
          ? error.message
          : 'Unknown audio error';
    // @ts-ignore
    this.$server?.reportError(message);
  }
}

customElements.define('audio-client-bridge', AudioClientBridgeElement);
