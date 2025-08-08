import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { vi, describe, test, expect, beforeEach } from 'vitest';

// Mock the auth context before importing Login
const mockLogin = vi.fn();
const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock the auth context
vi.mock('../../context/authContext', () => ({
  useAuth: () => ({
    login: mockLogin,
    user: null,
    logout: vi.fn(),
    isAuthenticated: false,
    loading: false,
  }),
}));

// Import Login after mocking
import Login from '../Login';

const renderLogin = () => {
  return render(
    <BrowserRouter>
      <Login />
    </BrowserRouter>
  );
};

describe('Login Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('renders login form', () => {
    renderLogin();
    
    expect(screen.getByText('City Note')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Login' })).toBeInTheDocument();
    expect(screen.getByText('Sign up')).toBeInTheDocument();
  });

  test('handles form submission with valid credentials', async () => {
    mockLogin.mockResolvedValueOnce(undefined);
    renderLogin();

    // Find inputs by their name attributes
    const inputs = screen.getAllByDisplayValue('');
    expect(inputs).toHaveLength(2); // Username and password inputs
    
    const usernameInput = inputs[0]; // First empty input is username
    const passwordInput = inputs[1]; // Second empty input is password
    
    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(passwordInput, { target: { value: 'testpass' } });
    
    const submitButton = screen.getByRole('button', { name: 'Login' });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('testuser', 'testpass');
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  test('displays error message on login failure', async () => {
    mockLogin.mockRejectedValueOnce(new Error('Invalid credentials'));
    renderLogin();

    const inputs = screen.getAllByDisplayValue('');
    expect(inputs).toHaveLength(2);
    
    const usernameInput = inputs[0];
    const passwordInput = inputs[1];
    
    fireEvent.change(usernameInput, { target: { value: 'wronguser' } });
    fireEvent.change(passwordInput, { target: { value: 'wrongpass' } });
    
    const submitButton = screen.getByRole('button', { name: 'Login' });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Invalid username or password')).toBeInTheDocument();
    });
  });
}); 