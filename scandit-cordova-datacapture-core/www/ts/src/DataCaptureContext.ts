/// <amd-module name="scandit-cordova-datacapture-core.DataCaptureContext"/>
// ^ needed because Cordova can't resolve "../xx" style dependencies
import { FrameSource } from './Camera+Related';
import { DataCaptureContextProxy } from './Cordova/DataCaptureContextProxy';
import { DataCaptureContextListener } from './DataCaptureContext+Related';
import { DataCaptureView } from './DataCaptureView';
import { DefaultSerializeable, ignoreFromSerialization, nameForSerialization, Serializeable } from './Serializeable';

export interface PrivateDataCaptureMode {
  _context: Optional<DataCaptureContext>;
}

export interface DataCaptureMode extends Serializeable {
  isEnabled: boolean;
  readonly context: DataCaptureContext | null;
}

export interface PrivateDataCaptureContext {
  modes: [DataCaptureMode];
  initialize: () => void;
  update: () => Promise<void>;
}

export interface DataCaptureContextCreationOptions {
  deviceName: Optional<string>;
}

export class DataCaptureContext extends DefaultSerializeable {
  @nameForSerialization('frameSource')
  private _frameSource: Optional<FrameSource> = null;

  private view: Optional<DataCaptureView> = null;

  private modes: DataCaptureMode[] = [];

  @ignoreFromSerialization
  private proxy: DataCaptureContextProxy;
  @ignoreFromSerialization
  private listeners: DataCaptureContextListener[] = [];

  // TODO: adjust when readding framedata to the api https://jira.scandit.com/browse/SDC-1159
  // @ignoreFromSerialization
  // private frameListeners: DataCaptureContextFrameListener[] = [];

  public get frameSource(): Optional<FrameSource> {
    return this._frameSource;
  }

  public static forLicenseKey(licenseKey: string): DataCaptureContext {
    return DataCaptureContext.forLicenseKeyWithOptions(licenseKey, null);
  }

  public static forLicenseKeyWithOptions(
    licenseKey: string, options: Optional<DataCaptureContextCreationOptions>): DataCaptureContext {
    return new DataCaptureContext(licenseKey, (options || {}).deviceName || '');
  }

  private constructor(
    private licenseKey: string,
    private deviceName: string,
  ) { super(); }

  public setFrameSource(frameSource: Optional<FrameSource>): Promise<void> {
    this._frameSource = frameSource;
    if (frameSource) {
      (frameSource as any).context = this;
    }
    return this.update();
  }

  public addListener(listener: Optional<DataCaptureContextListener>): void {
    if (listener == null) {
      return;
    }

    if (this.listeners.includes(listener)) {
      return;
    }
    this.listeners.push(listener);
  }

  public removeListener(listener: Optional<DataCaptureContextListener>): void {
    if (listener == null) {
      return;
    }

    if (!this.listeners.includes(listener)) {
      return;
    }
    this.listeners.splice(this.listeners.indexOf(listener), 1);
  }

  // TODO: adjust when readding framedata to the api https://jira.scandit.com/browse/SDC-1159
  // public addFrameListener(frameListener: DataCaptureContextFrameListener) {
  //   if (this.frameListeners.includes(frameListener)) {
  //     return;
  //   }
  //   this.frameListeners.push(frameListener);
  // }

  // TODO: adjust when readding framedata to the api https://jira.scandit.com/browse/SDC-1159
  // public removeFrameListener(frameListener: DataCaptureContextFrameListener) {
  //   if (!this.frameListeners.includes(frameListener)) {
  //     return;
  //   }
  //   this.frameListeners.splice(this.frameListeners.indexOf(frameListener), 1);
  // }

  public addMode(mode: DataCaptureMode): void {
    if (!this.modes.includes(mode)) {
      this.modes.push(mode);
      (mode as any as PrivateDataCaptureMode)._context = this;
      this.update();
    }
  }

  public removeMode(mode: DataCaptureMode): void {
    if (this.modes.includes(mode)) {
      this.modes.splice(this.modes.indexOf(mode), 1);
      (mode as any as PrivateDataCaptureMode)._context = null;
      this.update();
    }
  }

  public removeAllModes(): void {
    this.modes = [];
    this.update();
  }

  public dispose(): void {
    if (!this.proxy) {
      return;
    }
    this.proxy.dispose();
  }

  private initialize(): void {
    if (this.proxy) {
      return;
    }
    this.proxy = DataCaptureContextProxy.forDataCaptureContext(this);
  }

  private update(): Promise<void> {
    if (!this.proxy) {
      return Promise.resolve();
    }
    return this.proxy.updateContextFromJSON();
  }
}
