# EduStream Hub

A Spring MVC + JSP web application for schools and teachers to collect, organise and play
educational videos, connected to YouTube.

## Features

- **Accounts and roles**: login / logout / registration; administrators and teachers see different pages
- **YouTube integration**
  - Search YouTube (safe search, embeddable videos only) with result pages and an in-page preview player
  - Add a video by pasting any YouTube link (`watch`, `youtu.be`, `shorts`, `embed`, `live`);
    title, description, channel and duration are filled in automatically
  - Import a whole YouTube playlist (up to 200 videos) in one step
  - Videos play inside the site (privacy-enhanced `youtube-nocookie.com` player) and show live view and like counts
- **Classes**: teachers create classes, add videos and files to them, and play all YouTube videos of a class in a row
- **Content library**: upload images and videos (up to 200 MB), search and filter, edit and delete
- **Schools**: add, edit and delete schools (administrators)
- **Dashboards and reports** with live statistics and recent activity

## Requirements

- JDK 8 or newer
- Maven 3
- Apache Tomcat 9 (Spring 5 uses the `javax.servlet` API, so Tomcat 10+ is **not** supported)

## Build and run

```bash
mvn clean package
```

Deploy `target/EduStreamHub.war` to Tomcat 9 (copy it into `webapps/`), or import the folder into
Eclipse as an *Existing Maven Project* and run it on a Tomcat 9 server.
Then open <http://localhost:8080/EduStreamHub/>.

## Demo accounts

| Role          | Username  | Password  |
|---------------|-----------|-----------|
| Administrator | `admin`   | `admin`   |
| Teacher       | `teacher` | `teacher` |

New accounts can be created on the registration page; they log in with their email address.

## Connecting YouTube (API key)

Adding videos by link and playing them works without any setup. Searching YouTube, importing playlists,
filling in video details and showing view counts use the **YouTube Data API v3**, which needs a free API key:

1. Open the [Google Cloud Console](https://console.cloud.google.com/) and create a project.
2. Go to *APIs & Services → Library*, search for **YouTube Data API v3** and click *Enable*.
3. Go to *APIs & Services → Credentials → Create credentials → API key*.
   (Recommended: restrict the key to the YouTube Data API v3.)
4. Give the key to the application in one of these ways:
   - environment variable: `YOUTUBE_API_KEY=your-key`
   - JVM option when starting Tomcat: `-Dyoutube.api.key=your-key`
     (in Eclipse: *Servers* view → double-click Tomcat → *Open launch configuration* → *Arguments* → *VM arguments*)
5. Restart Tomcat. The administrator dashboard shows "YouTube API: connected".

Do not commit the key to Git. The free quota is 10,000 units per day; one search costs 100 units,
looking up videos or playlists costs 1 unit.

## Notes

- All data is kept in memory and is reset when the server restarts.
- Uploaded files are saved to `<java.io.tmpdir>/edustream-uploads` by default.
  Start Tomcat with `-Dedustream.upload.dir=/path/to/folder` to use a different folder.
