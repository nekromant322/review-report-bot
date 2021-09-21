- deploy on heroku  
  mvn clean package  
  heroku deploy:jar target/telegram-0.0.2-SNAPSHOT.jar --app mentoring-review-bot


- logs  
  heroku logs --tail --app mentoring-review-bot


- manage  
  heroku run bash --app mentoring-review-bot

