import { Component, input, computed } from '@angular/core';

@Component({
  selector: 'app-avatar',
  standalone: true,
  templateUrl: './avatar.component.html',
  styleUrl: './avatar.component.scss'
})
export class AvatarComponent {
  nickname = input.required<string>();
  size = input(36);
  variant = input<'default' | 'reporter' | 'you'>('default');

  initial = computed(() => this.nickname().charAt(0).toUpperCase());
  sizePx = computed(() => `${this.size()}px`);
  fontSize = computed(() => `${this.size() * 0.4}px`);
}
