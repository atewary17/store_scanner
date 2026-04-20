// app/javascript/ocr_uploader.js

export async function captureAndAnalyze(videoElement) {
  // Capture frame from camera
  const canvas = document.createElement('canvas');
  canvas.width = videoElement.videoWidth;
  canvas.height = videoElement.videoHeight;
  canvas.getContext('2d').drawImage(videoElement, 0, 0);

  // Convert to blob
  const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', 0.85));

  // Send to Rails
  const formData = new FormData();
  formData.append('photo', blob, 'label.jpg');
  formData.append('authenticity_token', getCSRFToken());

  const response = await fetch('/scans/ocr_lookup', {
    method: 'POST',
    body: formData
  });

  return await response.json();
}

function getCSRFToken() {
  return document.querySelector('meta[name="csrf-token"]')?.content;
}