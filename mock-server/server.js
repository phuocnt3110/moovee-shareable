const express = require('express');
const cors = require('cors');
const path = require('path');
const app = express();
const port = 3000;

app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

const getBaseUrl = (req) => `${req.protocol}://${req.get('host')}`;

const movies = require('./api/v1/movie/list/data.json');


// Helper to add base URL to movie videoUrls
const formatMovieResponse = (movie, baseUrl) => ({
  ...movie,
  videoUrls: movie.videoUrls.map(url => url.startsWith('/') ? `${baseUrl}${url}` : url)
});

// 1. GET movies with pagination
app.get('/api/v1/movie/list', (req, res) => {
  const page = parseInt(req.query.page) || 1;
  const pageSize = parseInt(req.query.pageSize) || 10;
  
  const start = (page - 1) * pageSize;
  const end = start + pageSize;
  
  console.log(`Pagination: page=${page}, pageSize=${pageSize}, start=${start}, end=${end}, total=${movies.length}`);
  
  const baseUrl = getBaseUrl(req);
  const paginatedMovies = movies.slice(start, end);
  res.json(paginatedMovies.map(m => formatMovieResponse(m, baseUrl)));
});

// 2. GET trending movies
app.get('/api/v1/movie/trending', (req, res) => {
  const baseUrl = getBaseUrl(req);
  res.json(movies.slice(0, 3).map(m => formatMovieResponse(m, baseUrl)));
});

// 3. GET movie recommendations
app.get('/api/v1/movie/recommendations', (req, res) => {
  const baseUrl = getBaseUrl(req);
  res.json(movies.slice(2, 5).map(m => formatMovieResponse(m, baseUrl)));
});

// 4. GET search movies
app.get('/api/v1/movie/search', (req, res) => {
  const { keyword } = req.query;
  const page = parseInt(req.query.page) || 1;
  const pageSize = parseInt(req.query.pageSize) || 10;
  
  let filteredMovies = movies;

  if (keyword) {
    filteredMovies = movies.filter(m =>
      m.title.toLowerCase().includes(keyword.toLowerCase()) ||
      m.description.toLowerCase().includes(keyword.toLowerCase())
    );
  }

  const start = (page - 1) * pageSize;
  const end = start + pageSize;
  
  console.log(`Search Pagination: page=${page}, pageSize=${pageSize}, start=${start}, end=${end}, totalFiltered=${filteredMovies.length}`);
  
  const baseUrl = getBaseUrl(req);
  res.json(filteredMovies.slice(start, end).map(m => formatMovieResponse(m, baseUrl)));
});

// 5. GET movie detail
app.get('/api/v1/movie/detail', (req, res) => {
  const { id } = req.query;
  const movie = movies.find(m => m.id === id);
  if (movie) {
    const baseUrl = getBaseUrl(req);
    res.json([formatMovieResponse(movie, baseUrl)]);
  } else {
    res.status(404).json({ error: 'Movie not found' });
  }
});

// 6. GET movie video
app.get('/api/v1/movie/video', (req, res) => {
  const { id, episode } = req.query;
  const movie = movies.find(m => m.id === id);
  if (movie) {
    const baseUrl = getBaseUrl(req);
    res.json([formatMovieResponse(movie, baseUrl)]);
  } else {
    res.status(404).json({ error: 'Movie not found' });
  }
});

app.listen(port, () => {
  console.log(`Mock API server running at http://localhost:${port}`);
});

