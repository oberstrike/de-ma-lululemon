import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Toast } from 'primeng/toast';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Toast],
  template: `
    <p-toast position="bottom-right" />
    <div class="app-container">
      <router-outlet />
    </div>
  `,
  styles: [
    `
      .app-container {
        min-height: 100vh;
      }

      :host ::ng-deep .p-toast {
        .p-toast-message {
          backdrop-filter: blur(10px);
          border: 1px solid var(--border-subtle);
        }

        .p-toast-message-success {
          background: var(--toast-success);
        }

        .p-toast-message-info {
          background: var(--toast-info);
        }

        .p-toast-message-warn {
          background: var(--toast-warn);
        }

        .p-toast-message-error {
          background: var(--toast-error);
        }
      }
    `,
  ],
})
export class AppComponent {}
