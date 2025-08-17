# City Note - Google Maps AI Integration

## Project Description
City Note is a city note application that integrates Google Maps and AI functionality, allowing users to create, view, and manage events on a map.

### Architecture
![Architecture](/readme-picture/Architecture.png)

### Database Schema
![Database Schema](/readme-picture/DB.png)

### Code Architecture
![Code Architecture](/readme-picture/CodeArch.png)

## Project Contributor
- Kaiyue Lin
- Lixing Chen

## Project Video
[Youtube Link](https://www.youtube.com/watch?v=CSGNxq31pf4)

## Quick Start

### Requirements
- Java 21+
- Node.js 18+
- Maven 3.6+
- MySQL 8.0+

### Local Development
1. Clone the project
```bash
git clone <repository-url>
cd city-note-gmap-AI
```

2. Setup your environment config
    
- Create a file `frontend/src/env.js`, do the following

```javascript
export const GMAP_MAP_ID = "MAP-ID-OF-YOUR-GOOGLE-API-ACCOUNT";
export const GMAP_API_KEY = "API-KEY-OF-YOUR-GOOGLE-API-ACCOUNT";
```

- Create a file `backend/.env`, do the following

```shell
openai.api-key = YOUR-OPEN-API-KEY
openai.base-url=https://api.openai.com/v1 # or your URL
jwt.secret=your-secret-key-here-make-it-long-and-secure-in-production
jwt.expiration=86400 # set up your own expiration time
```

3. Start local development environment
```bash
./dev.sh
```

### Deploy to EC2

#### 1. Configure SSH Key
Before deploying, you need to modify the SSH key configuration in the `deploy.sh` script:

```bash
vim deploy.sh

# Find the following line and modify it with your pem filename
SSH_KEY="~/.ssh/your-key.pem"  # Replace with your actual pem filename
```

**Supported path formats:**
- `~/.ssh/your-key.pem` - Relative path (recommended)
- `/absolute/path/to/your-key.pem` - Absolute path

#### 2. Run deployment script
```bash
./deploy.sh
```

The deployment script will automatically:
- Run tests
- Build the application
- Upload to EC2
- Configure services

## Project Structure
```
city-note-gmap-AI/
├── backend/                 # Spring Boot backend
├── frontend/               # React frontend
├── deploy.sh              # Deployment script
├── dev.sh                 # Local development script
└── README.md             # Project documentation
```

## Tech Stack
- **Backend**: Spring Boot, MySQL, JWT
- **Frontend**: React, TypeScript, Tailwind CSS
- **Maps**: Google Maps API
- **AI**: OpenAI API
- **Deployment**: AWS EC2, Nginx

## Contributing
1. Fork the project
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License
MIT License
