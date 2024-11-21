const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendMessage = functions.https.onRequest((req, res) => {
    const message = {
        notification: {
            title: "New Message",
            body: req.body.message
        },
        token: req.body.token
    };

    admin.messaging().send(message)
        .then(response => {
            res.status(200).send("Message sent successfully: " + response);
        })
        .catch(error => {
            res.status(500).send("Error sending message: " + error);
        });
});
