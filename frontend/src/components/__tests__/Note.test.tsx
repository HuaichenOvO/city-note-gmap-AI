import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { vi, describe, test, expect, beforeEach } from 'vitest';
import { Note } from '../Note';

// Mock NoteType
const mockNote = {
  noteId: '1',
  title: 'Test Note',
  content: 'This is a test note content that should be displayed in the component.',
  authorUsername: 'testuser',
  authorFirstName: 'John',
  authorLastName: 'Doe',
  date: new Date('2023-01-01'),
  county: 'Test County',
  eventType: 'RESTAURANT',
  likes: 5,
  pictureLinks: [],
};

const mockHandleNoteClick = vi.fn();

const renderNote = (note = mockNote) => {
  return render(
    <Note note={note} handleNoteClick={mockHandleNoteClick} />
  );
};

describe('Note Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('renders note with title and content', () => {
    renderNote();
    
    expect(screen.getByText('Test Note')).toBeInTheDocument();
    expect(screen.getByText('This is a test note content that should be displayed in the component.')).toBeInTheDocument();
  });

  test('displays full author name when first and last name are available', () => {
    renderNote();
    
    expect(screen.getByText('By John Doe')).toBeInTheDocument();
  });

  test('displays only first name when last name is not available', () => {
    const noteWithOnlyFirstName = {
      ...mockNote,
      authorLastName: '',
    };
    
    renderNote(noteWithOnlyFirstName);
    
    expect(screen.getByText('By John')).toBeInTheDocument();
  });

  test('displays username when no first or last name is available', () => {
    const noteWithOnlyUsername = {
      ...mockNote,
      authorFirstName: '',
      authorLastName: '',
    };
    
    renderNote(noteWithOnlyUsername);
    
    expect(screen.getByText('By testuser')).toBeInTheDocument();
  });

  test('calls handleNoteClick when View Details button is clicked', () => {
    renderNote();
    
    const viewDetailsButton = screen.getByRole('button', { name: 'View Details' });
    fireEvent.click(viewDetailsButton);
    
    expect(mockHandleNoteClick).toHaveBeenCalledWith(mockNote);
  });

  test('renders View Details button', () => {
    renderNote();
    
    expect(screen.getByRole('button', { name: 'View Details' })).toBeInTheDocument();
  });

  test('handles note with different content lengths', () => {
    const longContentNote = {
      ...mockNote,
      content: 'This is a very long content that should be truncated or handled appropriately by the component styling.',
    };
    
    renderNote(longContentNote);
    
    expect(screen.getByText(longContentNote.content)).toBeInTheDocument();
  });
}); 