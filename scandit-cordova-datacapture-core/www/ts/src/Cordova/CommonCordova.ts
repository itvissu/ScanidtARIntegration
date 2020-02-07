/// <amd-module name="scandit-cordova-datacapture-core.CommonCordova"/>
// ^ needed because Cordova can't resolve "../xx" style dependencies

// tslint:disable:no-var-requires
const exec = require('cordova/exec');
const channel = require('cordova/channel');
// tslint:enable:no-var-requires

export class CordovaError {
  public static fromJSON(json: any): Optional<CordovaError> {
    if (json && json.code && json.message) {
      return new CordovaError(json.code, json.message);
    } else {
      return null;
    }
  }

  constructor(
    public code: number,
    public message: string,
  ) { }
}

export const cordovaExec = (
  successCallback: Optional<Function>,
  errorCallback: Optional<Function>,
  className: string,
  functionName: string,
  args: Optional<[any]>,
) => {
  if ((window as any).Scandit && (window as any).Scandit.DEBUG) {
    // tslint:disable-next-line:no-console
    console.log(`Called native function: ${functionName}`, args, { success: successCallback, error: errorCallback });
  }
  const extendedSuccessCallback = (message: any) => {
    const shouldCallback = message && message.shouldNotifyWhenFinished;
    let callbackResult;
    if (successCallback) {
      callbackResult = successCallback(message);
    }
    if (shouldCallback) {
      exec(null, null, className, 'finishCallback', [callbackResult]);
    }
  };

  const extendedErrorCallback = (error: any) => {
    if (errorCallback) {
      const cordovaError = CordovaError.fromJSON(error);

      if (cordovaError !== null) {
        error = cordovaError;
      }

      errorCallback(error);
    }
  };

  exec(extendedSuccessCallback, extendedErrorCallback, className, functionName, args);
};

export const initializePlugin = (pluginName: string, customInitialization: Promise<void>) => {
  const readyEventName = `on${pluginName}Ready`;

  channel.createSticky(readyEventName);
  channel.waitForInitialization(readyEventName);

  customInitialization.then(() => channel[readyEventName].fire());
};
