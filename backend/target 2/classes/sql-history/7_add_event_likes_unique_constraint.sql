-- Add unique constraint to prevent duplicate likes from the same user on the same event
-- This script should be run after the event_likes table is created

-- First, remove any existing duplicate likes (if any)
DELETE el1 FROM event_likes el1
INNER JOIN event_likes el2 
WHERE el1.event_like_id > el2.event_like_id 
AND el1.event_id = el2.event_id 
AND el1.user_id = el2.user_id;

-- Add unique constraint
ALTER TABLE event_likes 
ADD CONSTRAINT uk_event_user_like UNIQUE (event_id, user_id); 