import { configureStore, createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';

// Описываем, что лежит в глобальном состоянии
interface AuthState {
  userId: string | null;
}

const initialState: AuthState = {
  // Инициализируем сразу из localStorage, чтобы при перезагрузке ничего не слетало
  userId: localStorage.getItem('userId'),
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    // Это вызываем, когда пользователь логинится
    setUserId: (state, action: PayloadAction<string | null>) => {
      state.userId = action.payload;
    },
  },
});

export const { setUserId } = authSlice.actions;

// Собираем наш Store
export const store = configureStore({
  reducer: {
    auth: authSlice.reducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;