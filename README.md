# 🚀 PixelPals – Trova il Tuo Eroe nel Mondo del Gaming! 🎮

Benvenuto su **PixelPals**, la tua destinazione definitiva per connetterti con altri giocatori e trasformare l'esperienza di gaming da solitaria a **epica**! Che tu stia cercando il compagno perfetto per le tue avventure online, un team per scalare le classifiche, o semplicemente qualcuno con cui condividere la passione, **PixelPals è dove le legende si incontrano.** 🧑‍🤝‍🧑

---

## ✨ Cosa Rende PixelPals Unico? ✨

PixelPals è stato progettato pensando ai gamer, offrendo strumenti intuitivi e potenti per forgiare connessioni significative e migliorare ogni sessione di gioco.

- **Identità da Gamer Completa**:

  - **Profili Personalizzabili**: Mostra al mondo chi sei! Crea il tuo profilo dettagliato con i **giochi preferiti**, il tuo **livello di abilità** (skill level), le **piattaforme** che usi e la tua **disponibilità oraria**.
  - **Matching Intelligente**: Non lasciare nulla al caso! Il nostro sistema di matching ti aiuta a trovare **compagni compatibili** basandosi sui tuoi gusti e stili di gioco.

- **Connettività Senza Limiti**:

  - **Cerca e Connettiti**: Sfrutta la **ricerca avanzata** per trovare giocatori in base a giochi specifici o piattaforme, e invia richieste di amicizia per espandere la tua rete.
  - **Chat Istantanea**: Comunica in tempo reale con i tuoi amici tramite la nostra **chat integrata** basata su WebSocket. Coordinare le tue strategie non è mai stato così facile!
  - **Stato Online Live**: Sai sempre chi è pronto a giocare! Visualizza lo **stato online dei tuoi amici in tempo reale**, per non perdere mai un'opportunità di gioco.

- **Progressi e Riconoscimenti**:
  - **Badge e Livelli Esclusivi**: Guadagna **badge** e scala i **livelli** man mano che giochi e interagisci, mostrando la tua dedizione alla community.
  - **Rating Post-Partita**: Valuta le tue esperienze di gioco con altri, contribuendo a costruire una community di fiducia e rispetto.

---

## 🛠️ Il Cuore Tecnologico di PixelPals ⚙️

Costruito con tecnologie all'avanguardia, PixelPals garantisce un'esperienza fluida, sicura e reattiva.

### 🌐 Frontend (L'Interfaccia che Ami)

- **React ⚛️**: Per un'interfaccia utente dinamica, performante e modulare, che offre un'esperienza utente reattiva e moderna.
- **Tailwind CSS 🎨**: Per uno styling rapido, personalizzabile e coerente, che garantisce un design accattivante e responsive in ogni angolo della piattaforma.

### 🧠 Backend (La Logica Dietro le Quinte)

- **Spring Boot + Maven ☕**: Un framework robusto e potente per costruire servizi backend scalabili e performanti, garantendo la stabilità dell'intera applicazione.
- **MongoDB 🍃**: Un database NoSQL flessibile e performante, ideale per gestire dati utente, profili e messaggi in modo efficiente.

### 🔗 Integrazioni e Sicurezza

- **JWT (JSON Web Tokens) 🔑**: Per un'autenticazione sicura e standardizzata, proteggendo i dati dei nostri utenti.
- **Google Mail**: Per la gestione delle comunicazioni e le verifiche account.
- **WebSocket (via STOMP)**: Per una comunicazione **real-time** tra utenti, abilitando la chat e gli aggiornamenti di stato online in modo istantaneo e bidirezionale.

  🚀 Prossimi Sviluppi: Cosa Ci Riserva il Futuro di PixelPals? 🌠
  Siamo costantemente al lavoro per migliorare e espandere l'esperienza di PixelPals. Ecco un'anteprima delle funzionalità che intendiamo implementare nelle prossime iterazioni per rendere la tua connessione con la community ancora più ricca e senza interruzioni:

Integrazione OAuth2 Avanzata: Miglioreremo il processo di login e registrazione introducendo l'autenticazione tramite Discord e Gmail, offrendo un accesso più rapido e sicuro.

Modalità di Recupero Account Sicura: Aggiungeremo funzionalità robuste per il recupero di password e nomi utente, garantendo che tu possa sempre accedere al tuo profilo senza problemi.

Esperienza Social Coerente: Perfezioneremo la visualizzazione dello stato online e la gestione della lista amici su tutte le pagine dell'applicazione, assicurando una coerenza e una reattività in tempo reale, ovunque tu sia su PixelPals.

---

Sei pronto a trovare la tua squadra dei sogni? **Accedi o Registrati ora su PixelPals e inizia la tua prossima avventura!** 🎉

qua sotto il file apllication
env.properties su richiesta

file application:

spring.application.name=pixelpals
spring.config.import=file:backend/env.properties
server.port=8080
logging.level.org.springframework.security.web.FilterChainProxy=DEBUG
logging.level.org.springframework.security.web.authentication=DEBUG
logging.level.org.springframework.security.web.access=DEBUG
logging.level.org.springframework.web.filter.CorsFilter=DEBUG
logging.level.com.pixelpals.backend=DEBUG
logging.level.org.springframework=DEBUG
spring.data.mongodb.uri=${MONGODB_URI}
spring.data.mongodb.database=${MONGODB_DATABASE_NAME}
cloudinary.cloud_name=${CLOUDINARY_NAME}
cloudinary.api_key=${CLOUDINARY_KEY}
cloudinary.api_secret=${CLOUDINARY_SECRET}
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${GMAIL_USER}
spring.mail.password=${GMAIL_PASS}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.debug=true
jwt.duration=900000 # 15 minuti in millisecondi
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION}
jwt.verification.expiration=86400000
![Screenshot dell'applicazione](<./frontend/src/assets/images/Screenshot (182).png>)
![Screenshot dell'applicazione](<./frontend/src/assets/images/Screenshot (183).png>)
![Screenshot dell'applicazione](<./frontend/src/assets/images/Screenshot (184).png>)
![Screenshot dell'applicazione](<./frontend/src/assets/images/Screenshot (185).png>)
![Screenshot dell'applicazione](<./frontend/src/assets/images/Screenshot (186).png>)
![Screenshot dell'applicazione](<./frontend/src/assets/images/Screenshot (187).png>)
![Screenshot dell'applicazione](<./frontend/src/assets/images/Screenshot (188).png>)
![Screenshot dell'applicazione](<./frontend/src/assets/images/Screenshot (189).png>)
![Screenshot dell'applicazione](<./frontend/src/assets/images/Screenshot (190).png>)
![Screenshot dell'applicazione](<./frontend/src/assets/images/Screenshot (191).png>)
![Screenshot dell'applicazione](<./frontend/src/assets/images/Screenshot (192).png>)
![Screenshot dell'applicazione](<./frontend/src/assets/images/Screenshot (193).png>)
![Screenshot dell'applicazione](<./frontend/src/assets/images/Screenshot (194).png>)
