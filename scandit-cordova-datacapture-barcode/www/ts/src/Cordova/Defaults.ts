/// <amd-module name="scandit-cordova-datacapture-barcode.Defaults"/>
// ^ needed because Cordova can't resolve "../xx" style dependencies
import {
  PrivateSymbologyDescription,
  PrivateSymbologySettings,
  SymbologyDescription,
  SymbologySettings,
} from 'Barcode';
import { CameraSettings, PrivateCameraSettings } from 'Camera+Related';
import { Color, PrivateColor } from 'Common';
import { CameraSettingsDefaultsJSON } from 'CoreDefaults';

export interface Defaults {
  BarcodeCaptureOverlay: {
    Brush: {
      fillColor: Color,
      strokeColor: Color,
      strokeWidth: number,
    },
  };

  BarcodeCaptureSettings: {
    codeDuplicateFilter: number,
  };

  SymbologySettings: {
    [key: string]: SymbologySettings,
  };

  SymbologyDescriptions: SymbologyDescription[];

  RecommendedCameraSettings: CameraSettings;
}

export interface DefaultsJSON {
  BarcodeCaptureOverlay: {
    Brush: {
      fillColor: string,
      strokeColor: string,
      strokeWidth: number,
    },
  };

  BarcodeCaptureSettings: {
    codeDuplicateFilter: number,
  };

  SymbologySettings: {
    [key: string]: string,
  };

  SymbologyDescriptions: string[];

  RecommendedCameraSettings: CameraSettingsDefaultsJSON;
}

export const defaultsFromJSON: (json: DefaultsJSON) => Defaults = (json: DefaultsJSON) => {
  return {
    BarcodeCaptureOverlay: {
      Brush: {
        fillColor: (Color as any as PrivateColor).fromJSON(json.BarcodeCaptureOverlay.Brush.fillColor),
        strokeColor: (Color as any as PrivateColor).fromJSON(json.BarcodeCaptureOverlay.Brush.strokeColor),
        strokeWidth: json.BarcodeCaptureOverlay.Brush.strokeWidth,
      },
    },

    BarcodeCaptureSettings: {
      codeDuplicateFilter: json.BarcodeCaptureSettings.codeDuplicateFilter,
    },

    SymbologySettings: Object.keys(json.SymbologySettings)
      .reduce((settings: { [key: string]: SymbologySettings }, identifier) => {
        settings[identifier] = (SymbologySettings as any as PrivateSymbologySettings)
          .fromJSON(JSON.parse(json.SymbologySettings[identifier]));
        return settings;
      }, { }),

    SymbologyDescriptions: json.SymbologyDescriptions.map(description =>
      (SymbologyDescription as any as PrivateSymbologyDescription).fromJSON(JSON.parse(description))),

    RecommendedCameraSettings: (CameraSettings as any as PrivateCameraSettings)
      .fromJSON(json.RecommendedCameraSettings),
  };
};
