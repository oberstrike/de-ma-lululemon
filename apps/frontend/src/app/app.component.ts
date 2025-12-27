import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MessageService } from 'primeng/api';
import { Toast } from 'primeng/toast';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Toast],
  providers: [MessageService],
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
          background: rgb(34 197 94 / 90%);
        }

        .p-toast-message-info {
          background: rgb(59 130 246 / 90%);
        }

        .p-toast-message-warn {
          background: rgb(234 179 8 / 90%);
        }

        .p-toast-message-error {
          background: rgb(239 68 68 / 90%);
        }
      }
    `,
  ],
})
export class AppComponent {}
