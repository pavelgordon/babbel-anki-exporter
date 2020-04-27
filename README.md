## Babbel2Anki
Fetches vocabulary from [Babbel](babbel.com) and exports it to Anki-compatible deck.  
Better to use with [babbel-chrome-extension](https://github.com/pavelgordon/babbel-chrome-extension), but can work standalone.
## Tech/framework used
- Kotlin/Ktor
- Gradle
## Installation
- Install Kotlin
- Install Gradle
- Install Anki Desktop
- Clone project to local machine.
## How to run?
### Via IntelliJ
* Run `Application.kt`
* Or run IntelliJ profile from `.run` directory
#### Via Jar
* `./gradlew build`
* `java -jar build/libs/babbel2anki-0.0.1.jar`
## How to use?
`Babbel2Anki` has one endpoint: `POST localhost:8080/dictionary/download`

Body: 
```
{
    "url": "https://api.babbel.io/gamma/v5/en/accounts/4534c7db8cjc475793477185d114998p/learn_languages/ITA/learned_items",
    "deckName": "ITALIAN", 
    "learned_items": [
        {
            "id": "bbd83343815846eeb454b42202e6d926",
            "display_language_text": "What will you have?",
            "learn_language_text": "Tu cosa prendi?",
            "times_reviewed": 3,
            "last_reviewed_at": "2020-04-11T09:30:38Z",
            "knowledge_level": 2,
            "next_review_at": "2020-04-18",
            "created_at": "2020-04-01T11:01:00Z",
            "image": {
                "id": "f94032b1573e4f069882beb65c1fdb1c"
            },
            "sound": {
                "id": "770973f99b53425cbb17de68ab1964e0"
            }
        }]
}
```
- `url` - optional property, used to parse deck name(e.g. ITA) from Babbel API.
- `deckName` - optional property, used to parse deck name(e.g. ITALIAN)
- `learned_items` - array of objects, each object corresponds to a specific word/card.  

`Babbel2Anki` 
- converts such json into a file `deck/<DECK_NAME>.csv` with following format:  
`<learnLanguageText>|<img src='<imageId>.png'/>|<displayLanguageText>>|[sound:<audioId>.mp3]`. E.g.:  
`Tu cosa prendi?|<img src='f94032b1573e4f069882beb65c1fdb1c.png'/>|What will you have?|[sound:770973f99b53425cbb17de68ab1964e0.mp3]`
- downloads all resources(images and audios) to `collection.media` folder(e.g.: `"/Users/pavelgordon/Library/Application Support/Anki2/Pavel/collection.media"`)
## How to import to Anki
- During import use delimiter `|`.
- TBD

## Plans
- Use AnkiConnect to import directly to Anki deck

## Contact
Feel free to shoot a message at `gordon.pav@gmail.com`
