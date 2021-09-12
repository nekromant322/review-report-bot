deploy on heroku mvn clean package heroku deploy:jar target/telegram-0.0.1-SNAPSHOT.jar --app mentoring-review-bot

logs  
heroku logs --tail --app mentoring-review-bot

