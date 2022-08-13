import CssBaseline from '@mui/material/CssBaseline';
import React, { Suspense, lazy } from 'react';
import { createRoot } from 'react-dom/client';
import '@fontsource/jetbrains-mono/400.css';
import '@fontsource/jetbrains-mono/400-italic.css';
import '@fontsource/jetbrains-mono/700.css';
import '@fontsource/jetbrains-mono/700-italic.css';
import '@fontsource/jetbrains-mono/variable.css';
import '@fontsource/jetbrains-mono/variable-italic.css';
import '@fontsource/roboto/300.css';
import '@fontsource/roboto/300-italic.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/400-italic.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/500-italic.css';
import '@fontsource/roboto/700.css';
import '@fontsource/roboto/700-italic.css';

import Loading from './Loading';
import RootStore, { RootStoreProvider } from './RootStore';
import ThemeProvider from './theme/ThemeProvider';
import getLogger from './utils/getLogger';

const log = getLogger('index');

const initialValue = `class Family {
    contains Person[] members
}

class Person {
    Person[] children opposite parent
    Person[0..1] parent opposite children
    int age
    TaxStatus taxStatus
}

enum TaxStatus {
    child, student, adult, retired
}

% A child cannot have any dependents.
pred invalidTaxStatus(Person p) <->
    taxStatus(p, child),
    children(p, _q)
  ; taxStatus(p, retired),
    parent(p, q),
    !taxStatus(q, retired).

rule createChild(may Person p, must Person newPerson):
    may children(p, newPerson),
    may !equals(newPerson, newPerson)
==> new q <: newPerson,
    children(p, q),
    taxStatus(q, child).

indiv family.
Family(family).
members(family, anne).
members(family, bob).
members(family, ciri).
children(anne, ciri).
?children(bob, ciri).
default children(ciri, *): false.
taxStatus(anne, adult).
age(anne, 35).
bobAge: 27.
age(bob, bobAge).
!age(ciri, bobAge).

scope Family = 1, Person += 5..10.
`;

const rootStore = new RootStore(initialValue);

const App = lazy(() => import('./App.js'));

const app = (
  <React.StrictMode>
    <RootStoreProvider rootStore={rootStore}>
      <ThemeProvider>
        <CssBaseline enableColorScheme />
        <Suspense fallback={<Loading />}>
          <App />
        </Suspense>
      </ThemeProvider>
    </RootStoreProvider>
  </React.StrictMode>
);

const rootElement = document.getElementById('app');
if (rootElement === null) {
  log.error('Root element not found');
} else {
  const root = createRoot(rootElement);
  root.render(app);
}