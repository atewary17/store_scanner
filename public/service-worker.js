const CACHE = 'store-scanner-v1';
const OFFLINE_URLS = ['/', '/offline'];

self.addEventListener('install', e => {
  e.waitUntil(
    caches.open(CACHE).then(cache => cache.addAll(OFFLINE_URLS))
  );
});

self.addEventListener('fetch', e => {
  e.respondWith(
    fetch(e.request).catch(() => caches.match(e.request))
  );
});