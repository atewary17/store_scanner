import { BrowserMultiFormatReader } from '@zxing/browser';

const codeReader = new BrowserMultiFormatReader();

export function startScanner(videoElementId, onScanSuccess) {
  codeReader.decodeFromVideoDevice(null, videoElementId, (result, err) => {
    if (result) {
      onScanSuccess(result.getText(), result.getBarcodeFormat().toString());
      stopScanner();
    }
  });
}

export function stopScanner() {
  codeReader.reset();
}