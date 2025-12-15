# Render Deployment Guide

This guide explains how to deploy the Habit Tracker backend on Render using Docker.

## Prerequisites

- A Render account
- Your Neon production database connection details
- Git repository with your code

## Render Configuration

### 1. Create a New Web Service

1. Go to your Render dashboard
2. Click "New +" → "Web Service"
3. Connect your Git repository
4. Select the repository and branch

### 2. Configure Build Settings

**Important:** Leave the build command **EMPTY** - the Dockerfile handles the build.

- **Name**: `habit-tracker` (or your preferred name)
- **Environment**: `Docker`
- **Dockerfile Path**: `Dockerfile` (default, in project root)
- **Docker Context**: `.` (default, project root)
- **Build Command**: *(Leave empty - Dockerfile handles this)*
- **Start Command**: *(Leave empty - Dockerfile handles this)*

### 3. Environment Variables

Add the following environment variables in Render's dashboard:

#### Required Variables

```
SPRING_PROFILES_ACTIVE=prod
```

#### Database Configuration (Neon Production)

⚠️ **IMPORTANT**: Use your actual production database credentials from Neon. Never commit these to git!

Spring Boot automatically recognizes these standard environment variable names:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://your-neon-prod-host:5432/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=your_neon_prod_username
SPRING_DATASOURCE_PASSWORD=your_neon_prod_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
```

**Note**: Get these values from your Neon production database dashboard. The connection string format should be:
`jdbc:postgresql://[host]:[port]/[database]?sslmode=require`

#### JWT Configuration

```
JWT_SECRET=your_strong_randomly_generated_secret_key_here
JWT_EXPIRATION=3600000
```

#### Port Configuration

⚠️ **IMPORTANT: Do NOT set `PORT` manually in Render!**

Render automatically sets the `PORT` environment variable for you. Your application is already configured to use it via `server.port=${PORT:8080}` in `application.properties`. 


### 4. Advanced Settings (Optional)

- **Auto-Deploy**: Enable to automatically deploy on git push
- **Health Check Path**: `/health` (dedicated health endpoint)
- **Plan**: Choose based on your needs (Free tier available)

## Environment Variables Summary

Here's a complete list of environment variables you need to **manually set** in Render:

| Variable | Value | Required |
|----------|-------|----------|
| `SPRING_PROFILES_ACTIVE` | `prod` | ✅ Yes |
| `SPRING_DATASOURCE_URL` | Your Neon production DB URL | ✅ Yes |
| `SPRING_DATASOURCE_USERNAME` | Your Neon DB username | ✅ Yes |
| `SPRING_DATASOURCE_PASSWORD` | Your Neon DB password | ✅ Yes |
| `SPRING_DATASOURCE_DRIVER_CLASS_NAME` | `org.postgresql.Driver` | ✅ Yes |
| `JWT_SECRET` | Your JWT secret key | ✅ Yes |
| `JWT_EXPIRATION` | `3600000` (1 hour) | ✅ Yes |
| `PORT` | *(Automatically provided by Render - **DO NOT SET THIS**)* | ❌ No |

## Deployment Steps

1. **Push your code** to your Git repository (if not already done)
2. **Create the Web Service** in Render with the settings above
3. **Add environment variables** as listed above
4. **Deploy** - Render will automatically build and deploy using the Dockerfile
5. **Wait for deployment** - First build may take 5-10 minutes
6. **Check logs** - Verify the application starts successfully

## Verifying Deployment

After deployment, check:

1. **Build logs**: Should show Maven build completing successfully
2. **Runtime logs**: Should show:
   - `Started HabitTrackerApplication`
   - `HikariPool-1 - Start completed`
   - No database connection errors
3. **Health check**: Test `GET /health` endpoint - should return `{"status":"UP"}`

## Troubleshooting

### Build Fails

- Check Dockerfile syntax
- Verify all dependencies in `pom.xml` are accessible
- Check build logs for specific Maven errors

### Application Won't Start

- Verify all environment variables are set correctly
- Check database connection (Neon production DB should be accessible)
- Review runtime logs for errors
- Ensure `SPRING_PROFILES_ACTIVE=prod` is set

### Database Connection Issues

- Verify Neon production database is accessible
- Check connection string format
- Ensure SSL is enabled (`sslmode=require`)
- Verify credentials are correct

### Port Issues

- **Do NOT manually set `PORT` in Render** - Render automatically provides this variable
- Your app uses `server.port=${PORT:8080}` which automatically picks up Render's port
- If you see port binding errors, ensure you haven't manually set `PORT` in environment variables

## Security Notes

⚠️ **Important Security Considerations:**

1. **Never commit secrets** - Use environment variables only
2. **JWT Secret**: Use a strong, randomly generated secret in production
3. **Database Password**: Keep it secure, never expose in logs
4. **Environment Variables**: Render encrypts them, but be careful with logs

## Next Steps

After successful deployment:

1. Test your API endpoints
2. Set up custom domain (optional)
3. Configure SSL/TLS (Render provides this automatically)
4. Monitor logs and performance
5. Set up database migrations if needed

## Support

- Render Docs: https://render.com/docs
- Spring Boot Docker: https://spring.io/guides/gs/spring-boot-docker/

