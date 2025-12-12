class AudioClientBridgeElement extends HTMLElement {
  private mediaStream?: MediaStream;
  private selectedInput?: string;
  private selectedOutput?: string;

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
    this.mediaStream?.getAudioTracks().forEach((track) => {
      track.enabled = !muted;
    });
    // @ts-ignore
    this.$server?.reportMute(muted);
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
