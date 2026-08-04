import { FaIconLibrary } from '@fortawesome/angular-fontawesome';

import { fontAwesomeIcons } from 'app/config/font-awesome-icons';

export function registerAllIcons(library: FaIconLibrary): void {
  library.addIcons(...fontAwesomeIcons);
}
