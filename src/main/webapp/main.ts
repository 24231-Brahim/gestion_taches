// Unregister any stale service worker from previous builds
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.getRegistrations().then(registrations => {
    for (const registration of registrations) {
      registration.unregister();
    }
  });
}

import('./bootstrap').catch((err: unknown) => console.error(err)); // NOSONAR
