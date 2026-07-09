import { Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';

@Component({
  selector: 'app-date-time-picker',
  standalone: true,
  imports: [FormsModule, NzDatePickerModule],
  templateUrl: './date-time-picker.component.html',
  styleUrl: './date-time-picker.component.scss'
})
export class DateTimePickerComponent {
  value = input<Date | null>(null);
  placeholder = input('Select date and time');

  valueChange = output<Date | null>();

  readonly showTimeConfig = {
    nzFormat: 'HH:mm',
    nzMinuteStep: 15,
    nzDefaultOpenValue: new Date(new Date().setHours(0, 0, 0, 0))
  };

  readonly dateFormat = 'yyyy-MM-dd HH:mm';

  onChange(date: Date | null) {
    this.valueChange.emit(date);
  }
}
