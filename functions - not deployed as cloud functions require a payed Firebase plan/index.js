/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {onRequest} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });


//My code below

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// Function to delete inactive games and trigger endGame for each deleted game
exports.deleteInactiveGames = functions.pubsub.schedule('every 24 hours').onRun(async (context) => {
    const now = Date.now();
    const inactiveDuration = 50 * 60 * 60 * 1000; // 50 hours in milliseconds

    const gamesRef = admin.database().ref('Games');
    const snapshot = await gamesRef.once('value');

    const updates = {};
    const gamesToDelete = [];

    snapshot.forEach((childSnapshot) => {
        const game = childSnapshot.val();
        if (!game.gameEnded && now - new Date(game.lastTurnTime).getTime() > inactiveDuration) {
            updates[childSnapshot.key] = null; // Mark for deletion

            // Determine winner and loser IDs
            const winnerId = game.next_player;
            const loserId = game.player_ids.find(id => id !== game.next_player);

            // Trigger endGame for each deleted game
            const endGamePromise = admin
                .firestore()
                .collection('Games')
                .doc(childSnapshot.key)
                .delete()
                .then(() => {
                    // Add back the game with winner and loser IDs
                    return admin
                        .firestore()
                        .collection('Games')
                        .doc(childSnapshot.key)
                        .set({
                            gameId: childSnapshot.key,
                            winnerId: winnerId,
                            loserId: loserId,
                        });
                });

            gamesToDelete.push(endGamePromise);
        }
    });

    // Wait for all endGame promises to complete
    await Promise.all(gamesToDelete);

    // Update scores and delete old invites
    await Promise.all([
        admin.database().ref('Games').update(updates),
        admin.firestore().collection('game_invitations').where('selectedTime', '<=', now - (14 * 24 * 60 * 60 * 1000)).get().then((snapshot) => {
            const batch = admin.firestore().batch();
            snapshot.forEach((doc) => {
                batch.delete(doc.ref);
            });
            return batch.commit();
        }),
    ]);

    return null;
});

// Function to update score when a game is deleted
exports.endGame = functions.https.onCall(async (data, context) => {
    const { gameId, winnerId } = data;

    try {
        const batch = admin.firestore().batch();
        const usersRef = admin.firestore().collection('users');

        // Increment winner's leaderboard
        if (winnerId !== "Unknown") {
            const winnerRef = usersRef.doc(winnerId);
            batch.update(winnerRef, { leaderboard: admin.firestore.FieldValue.increment(1) });
        }

        // Find and decrement the loser's leaderboard
        const gameRef = admin.firestore().collection('Games').doc(gameId);
        const gameSnapshot = await gameRef.get();
        const gameData = gameSnapshot.data();

        if (gameData) {
            const loserId = gameData.player_ids.find(id => id !== winnerId);
            if (loserId) {
                const loserRef = usersRef.doc(loserId);
                batch.update(loserRef, { leaderboard: admin.firestore.FieldValue.increment(-1) });
            }
        }

        // Remove the game
        batch.delete(gameRef);

        // Commit the batch
        await batch.commit();

        return { success: true };
    } catch (error) {
        console.error("Error ending game:", error);
        return { success: false, error: error.message };
    }
});

// Function to delete old invites
exports.deleteOldInvites = functions.pubsub.schedule('every day').onRun(async (context) => {
    const now = Date.now();
    const twoWeeksInMillis = 14 * 24 * 60 * 60 * 1000; // 2 weeks in milliseconds

    const invitesRef = admin.firestore().collection('game_invitations'); // Updated to match your collection name
    const snapshot = await invitesRef.get();

    const batch = admin.firestore().batch();

    snapshot.forEach(doc => {
        const invite = doc.data();
        if (now - doc.createTime.toDate().getTime() > twoWeeksInMillis) {
            batch.delete(doc.ref);
        }
    });

    return batch.commit();
});