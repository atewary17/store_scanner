// app/javascript/scanner_ui.js
import { BrowserMultiFormatReader } from '@zxing/browser';

const reader = new BrowserMultiFormatReader();
let currentBarcode     = '';
let currentBarcodeType = '';
let currentSource      = 'manual';

// ── Start / Stop scanning ─────────────────────────
window.startScanning = function () {
  setStatus('🔍 Scanning… point at barcode or QR code');
  reader.decodeFromVideoDevice(null, 'scanner-video', (result, err) => {
    if (result) {
      reader.reset();
      handleScan(result.getText(), result.getBarcodeFormat().toString());
    }
  }).catch(err => {
    setStatus('❌ Camera error: ' + (err?.message || err));
    console.error('Camera error:', err);
  });
};

window.stopScanning = function () {
  reader.reset();
  setStatus('Tap "Scan Barcode" to start');
};

// ── Core scan handler ─────────────────────────────
async function handleScan(barcode, format) {
  currentBarcode     = barcode;
  currentBarcodeType = format;
  navigator.vibrate && navigator.vibrate([80]);
  setStatus(`✓ Detected: ${barcode}`);

  openApprovalPanel({ barcode, barcode_type: format }, 'looking_up');

  try {
    const res  = await post('/scans/lookup', { barcode, barcode_type: format });
    const data = await res.json();

    if (data.found) {
      fillApprovalPanel(data.product, 'Your Database 💾', 'bg-blue-700');
    } else if (data.api_suggestion) {
      fillApprovalPanel(data.api_suggestion, `API: ${data.api_suggestion.source} 🌐`, 'bg-green-700');
      document.getElementById('ocr-trigger').classList.add('hidden');
    } else {
      fillApprovalPanel({ barcode, barcode_type: format }, 'Not Found ⚠️', 'bg-red-800');
      document.getElementById('ocr-trigger').classList.remove('hidden');
    }
  } catch (e) {
    setFieldValue('field-name', 'Network error — fill manually');
  }
}

// ── Approval panel ────────────────────────────────
function openApprovalPanel(data, sourceLabel, badgeClass) {
  document.getElementById('approval-panel').classList.remove('hidden');
  fillApprovalPanel(data, sourceLabel || 'Looking up…', badgeClass || 'bg-gray-700');
}

function fillApprovalPanel(data, sourceLabel, badgeClass) {
  currentSource = data.source || 'manual';

  // Badge
  const badge = document.getElementById('approval-source-badge');
  badge.textContent = sourceLabel;
  badge.className   = `text-xs font-bold px-3 py-1 rounded-full text-white ${badgeClass}`;

  // Fields
  setFieldValue('field-barcode',      data.barcode      || currentBarcode);
  setFieldValue('field-barcode-type', data.barcode_type || currentBarcodeType);
  setFieldValue('field-name',         data.name         || '');
  setFieldValue('field-brand',        data.brand        || '');
  setFieldValue('field-unit',         data.unit         || '');
  setFieldValue('field-description',  data.description  || '');

  // Category select
  const catSelect = document.getElementById('field-category');
  if (data.category) {
    const opt = [...catSelect.options].find(o =>
      o.value.toLowerCase() === data.category.toLowerCase()
    );
    if (opt) catSelect.value = opt.value;
  }
}

window.closeApproval = function () {
  document.getElementById('approval-panel').classList.add('hidden');
  document.getElementById('ocr-trigger').classList.add('hidden');
  setStatus('Ready — scan next item');
};

// ── OCR ───────────────────────────────────────────
window.captureForOCR = function () {
  document.getElementById('ocr-file-input').click();
};

window.handleOCRPhoto = async function (event) {
  const file = event.target.files[0];
  if (!file) return;

  setFieldValue('field-name', '⏳ Analysing with Google Vision…');

  const form = new FormData();
  form.append('photo',   file);
  form.append('barcode', currentBarcode);

  const res  = await fetch('/scans/ocr_lookup', {
    method:  'POST',
    headers: { 'X-CSRF-Token': csrfToken() },
    body:    form
  });
  const data = await res.json();

  if (data.success) {
    fillApprovalPanel(data.data, 'Google Vision OCR 🔬', 'bg-purple-700');
    document.getElementById('ocr-trigger').classList.add('hidden');
  } else {
    setFieldValue('field-name', '');
    alert('OCR could not read label — please type details manually.');
  }
};

// ── Save ──────────────────────────────────────────
window.saveProduct = async function () {
  const name = document.getElementById('field-name').value.trim();
  if (!name) {
    alert('Product name is required');
    return;
  }

  const sessionId = document.querySelector('[data-session-id]')?.dataset.sessionId;

  const product = {
    barcode:      document.getElementById('field-barcode').value,
    barcode_type: document.getElementById('field-barcode-type').value,
    name,
    brand:        document.getElementById('field-brand').value,
    category:     document.getElementById('field-category').value,
    sub_category: document.getElementById('field-sub-category').value,
    unit:         document.getElementById('field-unit').value,
    description:  document.getElementById('field-description').value,
    source:       currentSource
  };

  const btn = document.getElementById('btn-save');
  btn.textContent = 'Saving…';
  btn.disabled    = true;

  const res  = await post('/scans/save', { product, session_id: sessionId });
  const data = await res.json();

  btn.textContent = '✅ Save to Catalogue';
  btn.disabled    = false;

  if (data.success) {
    closeApproval();
    addItemToSessionList(data.scan_item);
    incrementSessionCount();
    setStatus(`✅ "${data.scan_item.name}" saved! Ready for next item.`);
  } else {
    alert('Error: ' + data.errors?.join(', '));
  }
};

// ── Session list helpers ──────────────────────────
function addItemToSessionList(item) {
  const list = document.getElementById('session-items');
  const el   = document.createElement('div');
  el.className = 'bg-gray-800 rounded-lg px-3 py-2 flex items-center gap-2';
  el.innerHTML = `
    <span class="text-lg">📦</span>
    <div class="flex-1 min-w-0">
      <p class="text-sm font-medium text-white truncate">${item.name}</p>
      <p class="text-xs text-gray-400">${item.brand || '—'} · ${item.category || '—'}</p>
    </div>
    <span class="font-mono text-xs text-gray-500">${item.barcode?.slice(-6)}</span>`;
  list.prepend(el);
}

function incrementSessionCount() {
  const badge = document.querySelector('.bg-blue-600.text-white.text-xs.font-bold');
  if (badge) {
    badge.textContent = (parseInt(badge.textContent) + 1) + ' items';
  }
}

// ── Utilities ─────────────────────────────────────
function setStatus(msg) {
  const el = document.getElementById('scan-status');
  if (el) el.textContent = msg;
}

function setFieldValue(id, value) {
  const el = document.getElementById(id);
  if (el) el.value = value;
}

function csrfToken() {
  return document.querySelector('meta[name="csrf-token"]')?.content;
}

function post(url, body) {
  return fetch(url, {
    method:  'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-CSRF-Token': csrfToken()
    },
    body: JSON.stringify(body)
  });
}