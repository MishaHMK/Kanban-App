import { Column } from "./column.model";

export interface Board {
  id: number;
  name: string;
  ownerId: number;
  columns: Column[];
  collaboratorIds: number[];
}