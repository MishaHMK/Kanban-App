import { Component, input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { User } from '../../../core/models/user.model';
import { AssigneePickerComponent } from '../../../shared/components/assignee-picker/assignee-picker.component';
import { DateTimePickerComponent } from '../../../shared/components/date-time-picker/date-time-picker.component';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NzFormModule,
    NzInputModule,
    NzSelectModule,
    NzDatePickerModule,
    AssigneePickerComponent,
    DateTimePickerComponent
  ],
  templateUrl: './task-form.component.html',
  styleUrl: './task-form.component.scss'
})
export class TaskFormComponent {
  form = input.required<FormGroup>();
  boardMembers = input<User[]>([]);

  priorities = [
    { label: 'Low', value: 'LOW' },
    { label: 'Medium', value: 'MEDIUM' },
    { label: 'High', value: 'HIGH' }
  ];

  onAssigneeChange(userId: number | null) {
    this.form().patchValue({ assigneeId: userId });
  }
}
