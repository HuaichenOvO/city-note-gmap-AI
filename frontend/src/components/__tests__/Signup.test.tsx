import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { vi, describe, test, expect, beforeEach } from 'vitest';

// Mock the auth API
const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock('../../api/authApi', () => ({
  authApi: {
    register: vi.fn(),
  },
}));

import Signup from '../Signup';
import { authApi } from '../../api/authApi';

const mockRegister = authApi.register as any;

const renderSignup = () => {
  return render(
    <BrowserRouter>
      <Signup />
    </BrowserRouter>
  );
};

describe('Signup Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('renders signup form', () => {
    renderSignup();
    
    expect(screen.getByRole('heading', { name: 'Sign Up' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Sign Up' })).toBeInTheDocument();
    expect(screen.getByText('Login')).toBeInTheDocument();
  });

  test('handles form submission with valid data', async () => {
    mockRegister.mockResolvedValueOnce(undefined);
    renderSignup();

    // Fill in the form using getAllByDisplayValue
    const inputs = screen.getAllByDisplayValue('');
    expect(inputs).toHaveLength(4); // Username, email, password, confirm password
    
    const usernameInput = inputs[0]; // Username
    const emailInput = inputs[1]; // Email
    const passwordInput = inputs[2]; // Password
    const confirmPasswordInput = inputs[3]; // Confirm Password
    
    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'testpass' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'testpass' } });
    
    const submitButton = screen.getByRole('button', { name: 'Sign Up' });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith({
        username: 'testuser',
        email: 'test@example.com',
        password: 'testpass',
        confirmPassword: 'testpass',
      });
    });

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
  });

  test('displays error when passwords do not match', async () => {
    renderSignup();

    // Fill in the form with mismatched passwords
    const inputs = screen.getAllByDisplayValue('');
    expect(inputs).toHaveLength(4);
    
    const usernameInput = inputs[0];
    const emailInput = inputs[1];
    const passwordInput = inputs[2];
    const confirmPasswordInput = inputs[3];
    
    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'testpass' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'differentpass' } });
    
    const submitButton = screen.getByRole('button', { name: 'Sign Up' });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Passwords do not match')).toBeInTheDocument();
    });
  });

  test('displays error message on registration failure', async () => {
    mockRegister.mockRejectedValueOnce(new Error('Registration failed'));
    renderSignup();

    // Fill in the form
    const inputs = screen.getAllByDisplayValue('');
    expect(inputs).toHaveLength(4);
    
    const usernameInput = inputs[0];
    const emailInput = inputs[1];
    const passwordInput = inputs[2];
    const confirmPasswordInput = inputs[3];
    
    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'testpass' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'testpass' } });
    
    const submitButton = screen.getByRole('button', { name: 'Sign Up' });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Registration failed. Please try again.')).toBeInTheDocument();
    });
  });
}); 