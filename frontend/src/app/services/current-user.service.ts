import { computed, Injectable, signal } from '@angular/core';

export interface CurrentUser {
  id: string;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class CurrentUserService {
  private readonly userSignal = signal<CurrentUser>({ id: 'user-1', name: 'Mock User' });

  readonly user = this.userSignal.asReadonly();
  readonly userId = computed(() => this.userSignal().id);

  setUser(user: CurrentUser): void {
    this.userSignal.set(user);
  }

  setUserId(id: string): void {
    this.userSignal.update((current) => ({ ...current, id }));
  }
}
