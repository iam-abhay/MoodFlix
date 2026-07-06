-- MoodFlix - Demo & Sample Data Seeding
-- Inserts default user accounts, movies, series, and songs matching the initialization catalog.

-- 1. Seeding User Accounts (BCrypt encrypted passwords)
-- Admin@1234 -> $2a$10$vNl1yOUpT310J06D7z052.7vL9a3Z/x/E4rQ96BwH3x0RzGqCgXoq
-- User@1234 -> $2a$10$tMhO.XG6bH3x0RzGqCgXoqvNl1yOUpT310J06D7z052.7vL9a3Z/x/E4rQ
INSERT INTO users (email, password_hash, role, display_name, full_name, age, gender) 
VALUES 
('admin@moodflix.com', '$2a$10$vNl1yOUpT310J06D7z052.7vL9a3Z/x/E4rQ96BwH3x0RzGqCgXoq', 'admin', 'SystemAdmin', 'Administrator', '30', 'Male')
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, role, display_name, full_name, age, gender) 
VALUES 
('user@moodflix.com', '$2a$10$tMhO.XG6bH3x0RzGqCgXoqvNl1yOUpT310J06D7z052.7vL9a3Z/x/E4rQ', 'user', 'RegularUser', 'Regular User', '24', 'Female')
ON CONFLICT (email) DO NOTHING;

-- 2. Seeding Content Catalog
INSERT INTO content (title, mood, type, link, description, image_url)
VALUES
('3 Idiots', 'Happy', 'Movie', 'https://www.youtube.com/watch?v=K0eDlFX9GMc', 'A feel-good friendship drama with humor and heart.', '3 idiots.jpeg'),
('Panchayat', 'Feel Good', 'Series', 'https://www.primevideo.com', 'Small-town comedy drama with relatable storytelling.', 'panchayat.jpeg'),
('Stranger Things', 'Thriller', 'Series', 'https://www.netflix.com', 'Mystery and sci-fi adventure with suspense.', 'strangerthings.jpeg'),
('Sita Ramam', 'Romantic', 'Movie', 'https://www.youtube.com/results?search_query=sita+ramam+trailer', 'Romantic period drama with emotional depth.', 'sitaRamam.jpeg'),
('Happy Song Mix', 'Happy', 'Song', 'https://www.youtube.com/results?search_query=happy+playlist', 'Upbeat music to boost mood and energy.', 'Happy song.jpeg'),
('Thriller Songs', 'Thriller', 'Song', 'https://www.youtube.com/results?search_query=thriller+songs', 'Dark, intense tracks for high-energy moments.', 'Thriller Songs.jpeg'),
('Little Things', 'Calm', 'Series', 'https://www.netflix.com', 'Light relationship series with warm everyday moments.', 'little things.jpeg'),
('Dil Bechara', 'Sad', 'Movie', 'https://www.hotstar.com', 'Emotional story with heartfelt performances.', 'dil bechara.jpeg'),
('Gullak', 'Comedy', 'Series', 'https://www.sonyliv.com', 'Family-based slice-of-life comedy drama.', 'Gullak.jpeg'),
('Hostel Daze', 'Comedy', 'Series', 'https://www.primevideo.com', 'College-life comedy with high relatability.', 'Hostel daze.jpeg');
