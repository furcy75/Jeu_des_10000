package fr.jeudes10000;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

final class GameDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "jeu_10000.db";
    private static final int DATABASE_VERSION = 5;
    private static final int TARGET_SCORE = 10000;
    private static final int OPENING_SCORE = 600;

    GameDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE games (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, " +
                "status TEXT NOT NULL DEFAULT 'active', winner_id INTEGER, " +
                "current_player_position INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE players (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "game_id INTEGER NOT NULL REFERENCES games(id) ON DELETE CASCADE, " +
                "name TEXT NOT NULL, position INTEGER NOT NULL, " +
                "opened INTEGER NOT NULL DEFAULT 0, failure_marks INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE score_entries (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "game_id INTEGER NOT NULL REFERENCES games(id) ON DELETE CASCADE, " +
                "player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE, " +
                "points INTEGER NOT NULL, kind TEXT NOT NULL, created_at INTEGER NOT NULL, " +
                "source_entry_id INTEGER REFERENCES score_entries(id) ON DELETE CASCADE, " +
                "failure_marks INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE INDEX score_entries_game ON score_entries(game_id, created_at)");
        db.execSQL("CREATE INDEX score_entries_source ON score_entries(source_entry_id)");
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE score_entries ADD COLUMN source_entry_id INTEGER " +
                    "REFERENCES score_entries(id) ON DELETE CASCADE");
            db.execSQL("CREATE INDEX score_entries_source ON score_entries(source_entry_id)");
            db.execSQL("UPDATE score_entries SET kind='legacy_theft', points=0 " +
                    "WHERE kind='theft' AND points>0");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE games ADD COLUMN current_player_position " +
                    "INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE players ADD COLUMN opened INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE players ADD COLUMN failure_marks INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE score_entries ADD COLUMN failure_marks " +
                    "INTEGER NOT NULL DEFAULT 0");
            db.execSQL("UPDATE players SET opened=1");
        }
        if (oldVersion < 4) {
            db.execSQL("UPDATE players SET opened=0 WHERE COALESCE(" +
                    "(SELECT SUM(e.points) FROM score_entries e " +
                    "WHERE e.player_id=players.id " +
                    "AND e.kind NOT IN ('legacy_theft','opening_ignored')), 0)<=0");
        }
        if (oldVersion < 5) {
            String winnerSubquery = "SELECT p.id FROM players p LEFT JOIN score_entries e " +
                    "ON e.player_id=p.id AND e.kind NOT IN ('legacy_theft','opening_ignored') " +
                    "WHERE p.game_id=games.id GROUP BY p.id " +
                    "HAVING COALESCE(SUM(e.points),0)=" + TARGET_SCORE +
                    " ORDER BY p.position LIMIT 1";
            db.execSQL("UPDATE games SET winner_id=(" + winnerSubquery +
                    "), status=CASE WHEN (" + winnerSubquery +
                    ") IS NULL THEN 'active' ELSE 'finished' END");
        }
    }

    long createGame(List<String> names) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        db.beginTransaction();
        try {
            ContentValues game = new ContentValues();
            game.put("created_at", now);
            game.put("updated_at", now);
            long gameId = db.insertOrThrow("games", null, game);
            for (int i = 0; i < names.size(); i++) {
                ContentValues player = new ContentValues();
                player.put("game_id", gameId);
                player.put("name", names.get(i));
                player.put("position", i);
                db.insertOrThrow("players", null, player);
            }
            db.setTransactionSuccessful();
            return gameId;
        } finally {
            db.endTransaction();
        }
    }

    List<Game> games(String status, int limit) {
        List<Game> result = new ArrayList<>();
        String selection = status == null ? null : "status=?";
        String[] args = status == null ? null : new String[]{status};
        try (Cursor cursor = getReadableDatabase().query("games",
                new String[]{"id", "created_at", "status", "winner_id", "current_player_position"},
                selection, args, null, null, "updated_at DESC", Integer.toString(limit))) {
            while (cursor.moveToNext()) {
                result.add(new Game(cursor.getLong(0), cursor.getLong(1),
                        cursor.getString(2), cursor.isNull(3) ? -1 : cursor.getLong(3),
                        cursor.getInt(4)));
            }
        }
        return result;
    }

    List<Player> players(long gameId) {
        List<Player> result = new ArrayList<>();
        String sql = "SELECT p.id, p.name, COALESCE(SUM(e.points), 0) " +
                "FROM players p LEFT JOIN score_entries e " +
                "ON e.game_id=p.game_id AND e.player_id=p.id " +
                "AND e.kind NOT IN ('legacy_theft','opening_ignored') " +
                "WHERE p.game_id=? GROUP BY p.id ORDER BY p.position";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{Long.toString(gameId)})) {
            while (cursor.moveToNext()) {
                result.add(new Player(cursor.getLong(0), cursor.getString(1), cursor.getInt(2)));
            }
        }
        return result;
    }

    int currentFailureMarks(long gameId, long playerId) {
        String sql = "SELECT s.failure_marks FROM score_entries s " +
                "WHERE s.game_id=? AND s.player_id=? AND s.kind='score' " +
                "AND NOT EXISTS (SELECT 1 FROM score_entries p " +
                "WHERE p.source_entry_id=s.id AND p.kind IN ('theft','three_failures')) " +
                "ORDER BY s.id DESC LIMIT 1";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{Long.toString(gameId), Long.toString(playerId)})) {
            if (cursor.moveToFirst()) return cursor.getInt(0);
        }
        try (Cursor cursor = getReadableDatabase().query("players", new String[]{"failure_marks"},
                "id=? AND game_id=?", new String[]{Long.toString(playerId), Long.toString(gameId)},
                null, null, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    List<Milestone> milestones(long gameId, long playerId) {
        List<Milestone> result = new ArrayList<>();
        String sql = "SELECT s.points, s.failure_marks, EXISTS(SELECT 1 FROM score_entries p " +
                "WHERE p.source_entry_id=s.id AND p.kind IN ('theft','three_failures')) " +
                ", (SELECT COALESCE(SUM(e.points), 0) FROM score_entries e " +
                "WHERE e.player_id=s.player_id AND e.id<=s.id " +
                "AND e.kind NOT IN ('legacy_theft','opening_ignored')) " +
                "FROM score_entries s WHERE s.game_id=? AND s.player_id=? AND s.kind='score' " +
                "ORDER BY s.id DESC LIMIT 20";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{Long.toString(gameId), Long.toString(playerId)})) {
            while (cursor.moveToNext()) {
                result.add(new Milestone(cursor.getInt(0), cursor.getInt(1),
                        cursor.getInt(2) != 0, cursor.getInt(3)));
            }
        }
        return result;
    }

    List<Entry> entries(long gameId) {
        List<Entry> result = new ArrayList<>();
        String sql = "SELECT e.id, p.name, e.points, e.kind, e.created_at, " +
                "(SELECT COALESCE(SUM(previous.points), 0) FROM score_entries previous " +
                "WHERE previous.player_id=e.player_id AND previous.id<=e.id " +
                "AND previous.kind NOT IN ('legacy_theft','opening_ignored')) " +
                "FROM score_entries e JOIN players p ON p.id=e.player_id " +
                "WHERE e.game_id=? ORDER BY e.created_at DESC, e.id DESC LIMIT 50";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{Long.toString(gameId)})) {
            while (cursor.moveToNext()) {
                result.add(new Entry(cursor.getLong(0), cursor.getString(1),
                        cursor.getInt(2), cursor.getString(3), cursor.getLong(4),
                        cursor.getInt(5)));
            }
        }
        return result;
    }

    EntryResult addEntry(long gameId, long playerId, int points, String kind) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        db.beginTransaction();
        boolean openingRejected = false;
        boolean failureThresholdReached = false;
        boolean failurePenalty = false;
        boolean exactGoalFailure = false;
        boolean resolveTies = false;
        List<Theft> thefts = new ArrayList<>();
        try {
            String storedKind = kind;
            int storedPoints = points;
            if (kind.equals("score") && points > 0) {
                boolean opened = isPlayerOpened(db, gameId, playerId);
                if (!opened && points < OPENING_SCORE) {
                    storedKind = "opening_ignored";
                    openingRejected = true;
                } else if ((long) playerScore(db, gameId, playerId) + points > TARGET_SCORE) {
                    storedKind = "failure";
                    storedPoints = 0;
                    exactGoalFailure = true;
                } else if (!opened) {
                    ContentValues open = new ContentValues();
                    open.put("opened", 1);
                    db.update("players", open, "id=? AND game_id=?",
                            new String[]{Long.toString(playerId), Long.toString(gameId)});
                }
            }

            ContentValues entry = new ContentValues();
            entry.put("game_id", gameId);
            entry.put("player_id", playerId);
            entry.put("points", storedPoints);
            entry.put("kind", storedKind);
            entry.put("created_at", now);
            long entryId = db.insertOrThrow("score_entries", null, entry);

            if (storedKind.equals("score")) {
                resolveTies = true;
            } else if (storedKind.equals("failure")) {
                FailureResult result = recordFailure(db, gameId, playerId, now);
                failureThresholdReached = result.thresholdReached;
                failurePenalty = result.penaltyApplied;
                if (result.palierId >= 0) {
                    ContentValues failure = new ContentValues();
                    failure.put("source_entry_id", result.palierId);
                    db.update("score_entries", failure, "id=?",
                            new String[]{Long.toString(entryId)});
                }
                resolveTies = true;
            }

            if (resolveTies) {
                thefts = applyTieCascade(db, gameId, playerId, now);
            }

            closePlayersAtOrBelowZero(db, gameId);
            ContentValues update = new ContentValues();
            update.put("updated_at", now);
            db.update("games", update, "id=?", new String[]{Long.toString(gameId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return new EntryResult(openingRejected, thefts,
                failureThresholdReached, failurePenalty, exactGoalFailure);
    }

    private boolean isPlayerOpened(SQLiteDatabase db, long gameId, long playerId) {
        try (Cursor cursor = db.query("players", new String[]{"opened"},
                "id=? AND game_id=?", new String[]{Long.toString(playerId), Long.toString(gameId)},
                null, null, null)) {
            return cursor.moveToFirst() && cursor.getInt(0) != 0;
        }
    }

    private FailureResult recordFailure(SQLiteDatabase db, long gameId, long playerId,
                                        long createdAt) {
        long palierId = latestActiveScoreId(db, gameId, playerId);
        if (palierId < 0) {
            int marks = playerFailureMarks(db, playerId);
            int updatedMarks = Math.min(3, marks + 1);
            ContentValues update = new ContentValues();
            update.put("failure_marks", updatedMarks);
            db.update("players", update, "id=?", new String[]{Long.toString(playerId)});
            return new FailureResult(updatedMarks == 3 && marks < 3, false, -1);
        }

        int marks = 0;
        try (Cursor cursor = db.query("score_entries", new String[]{"failure_marks"},
                "id=?", new String[]{Long.toString(palierId)}, null, null, null)) {
            if (cursor.moveToFirst()) marks = cursor.getInt(0);
        }
        int updatedMarks = Math.min(3, marks + 1);
        ContentValues update = new ContentValues();
        update.put("failure_marks", updatedMarks);
        db.update("score_entries", update, "id=?", new String[]{Long.toString(palierId)});

        boolean thresholdReached = updatedMarks >= 3;
        boolean penaltyApplied = thresholdReached &&
                addPenaltyForScore(db, gameId, playerId, palierId,
                        "three_failures", createdAt) > 0;
        return new FailureResult(thresholdReached, penaltyApplied, palierId);
    }

    private int playerFailureMarks(SQLiteDatabase db, long playerId) {
        try (Cursor cursor = db.query("players", new String[]{"failure_marks"},
                "id=?", new String[]{Long.toString(playerId)}, null, null, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    List<Theft> resolveExistingTies(long gameId) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        List<Theft> thefts = new ArrayList<>();
        db.beginTransaction();
        try {
            try (Cursor cursor = db.query("games", new String[]{"status"}, "id=?",
                    new String[]{Long.toString(gameId)}, null, null, null)) {
                if (cursor.moveToFirst() && cursor.getString(0).equals("finished")) {
                    db.setTransactionSuccessful();
                    return thefts;
                }
            }
            List<Long> playerIds = new ArrayList<>();
            // Reconcile old ties from the most recent score-changing player first.
            String playerOrder = "(SELECT COALESCE(MAX(e.id), 0) FROM score_entries e " +
                    "WHERE e.game_id=players.game_id AND e.player_id=players.id " +
                    "AND e.points!=0 AND e.kind NOT IN ('legacy_theft','opening_ignored')) DESC, " +
                    "position";
            try (Cursor cursor = db.query("players", new String[]{"id"}, "game_id=?",
                    new String[]{Long.toString(gameId)}, null, null, playerOrder)) {
                while (cursor.moveToNext()) playerIds.add(cursor.getLong(0));
            }
            for (long playerId : playerIds) {
                thefts.addAll(applyTieCascade(db, gameId, playerId, now));
            }
            closePlayersAtOrBelowZero(db, gameId);
            long winnerId = winnerIdForGame(db, gameId);
            boolean outcomeChanged = false;
            try (Cursor cursor = db.query("games", new String[]{"status", "winner_id"},
                    "id=?", new String[]{Long.toString(gameId)}, null, null, null)) {
                if (cursor.moveToFirst()) {
                    boolean currentlyFinished = cursor.getString(0).equals("finished");
                    long currentWinnerId = cursor.isNull(1) ? -1 : cursor.getLong(1);
                    outcomeChanged = currentlyFinished != (winnerId >= 0) ||
                            currentWinnerId != winnerId;
                }
            }
            if (!thefts.isEmpty() || outcomeChanged) {
                ContentValues update = new ContentValues();
                update.put("updated_at", now);
                if (winnerId >= 0) {
                    update.put("status", "finished");
                    update.put("winner_id", winnerId);
                } else {
                    update.put("status", "active");
                    update.putNull("winner_id");
                }
                db.update("games", update, "id=?", new String[]{Long.toString(gameId)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return thefts;
    }

    private List<Theft> applyTieCascade(SQLiteDatabase db, long gameId, long firstPlayerId,
                                        long createdAt) {
        ArrayDeque<Long> changedPlayers = new ArrayDeque<>();
        changedPlayers.add(firstPlayerId);
        List<Theft> thefts = new ArrayList<>();
        // A score tier can be stolen only once, so each queued step consumes a finite tier.
        while (!changedPlayers.isEmpty()) {
            long playerId = changedPlayers.removeFirst();
            int score = playerScore(db, gameId, playerId);
            for (long tiedPlayerId : playersAtScore(db, gameId, playerId, score)) {
                long palierId = latestActiveScoreId(db, gameId, tiedPlayerId);
                if (palierId >= 0) {
                    int pointsRemoved = addPenaltyForScore(
                            db, gameId, tiedPlayerId, palierId, "theft", createdAt);
                    if (pointsRemoved <= 0) continue;
                    thefts.add(new Theft(playerName(db, playerId),
                            playerName(db, tiedPlayerId), pointsRemoved));
                    changedPlayers.addLast(tiedPlayerId);
                }
            }
        }
        return thefts;
    }

    private int playerScore(SQLiteDatabase db, long gameId, long playerId) {
        String sql = "SELECT COALESCE(SUM(points), 0) FROM score_entries " +
                "WHERE game_id=? AND player_id=? AND kind NOT IN ('legacy_theft','opening_ignored')";
        try (Cursor cursor = db.rawQuery(sql,
                new String[]{Long.toString(gameId), Long.toString(playerId)})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    private void closePlayersAtOrBelowZero(SQLiteDatabase db, long gameId) {
        String sql = "UPDATE players SET opened=0 WHERE game_id=? AND opened!=0 AND COALESCE(" +
                "(SELECT SUM(e.points) FROM score_entries e " +
                "WHERE e.player_id=players.id " +
                "AND e.kind NOT IN ('legacy_theft','opening_ignored')), 0)<=0";
        db.execSQL(sql, new Object[]{gameId});
    }

    private long winnerIdForGame(SQLiteDatabase db, long gameId) {
        String sql = "SELECT p.id FROM players p LEFT JOIN score_entries e " +
                "ON e.player_id=p.id AND e.kind NOT IN ('legacy_theft','opening_ignored') " +
                "WHERE p.game_id=? GROUP BY p.id " +
                "HAVING COALESCE(SUM(e.points), 0)=? ORDER BY p.position LIMIT 1";
        try (Cursor cursor = db.rawQuery(sql,
                new String[]{Long.toString(gameId), Integer.toString(TARGET_SCORE)})) {
            return cursor.moveToFirst() ? cursor.getLong(0) : -1;
        }
    }

    private List<Long> playersAtScore(SQLiteDatabase db, long gameId, long playerId, int score) {
        List<Long> result = new ArrayList<>();
        String sql = "SELECT p.id, COALESCE(SUM(e.points), 0) " +
                "FROM players p LEFT JOIN score_entries e " +
                "ON e.game_id=p.game_id AND e.player_id=p.id " +
                "AND e.kind NOT IN ('legacy_theft','opening_ignored') " +
                "WHERE p.game_id=? GROUP BY p.id ORDER BY p.position";
        try (Cursor cursor = db.rawQuery(sql, new String[]{Long.toString(gameId)})) {
            while (cursor.moveToNext()) {
                if (cursor.getLong(0) != playerId && cursor.getInt(1) == score) {
                    result.add(cursor.getLong(0));
                }
            }
        }
        return result;
    }

    private long latestActiveScoreId(SQLiteDatabase db, long gameId, long playerId) {
        String sql = "SELECT s.id FROM score_entries s " +
                "WHERE s.game_id=? AND s.player_id=? AND s.kind='score' AND s.points>0 " +
                "AND NOT EXISTS (SELECT 1 FROM score_entries p " +
                "WHERE p.source_entry_id=s.id AND p.kind IN ('theft','three_failures')) " +
                "ORDER BY s.id DESC LIMIT 1";
        try (Cursor cursor = db.rawQuery(sql,
                new String[]{Long.toString(gameId), Long.toString(playerId)})) {
            return cursor.moveToFirst() ? cursor.getLong(0) : -1;
        }
    }

    private int addPenaltyForScore(SQLiteDatabase db, long gameId, long playerId,
                                   long scoreId, String kind, long createdAt) {
        int points;
        try (Cursor cursor = db.query("score_entries", new String[]{"points"},
                "id=? AND game_id=? AND player_id=? AND kind='score'",
                new String[]{Long.toString(scoreId), Long.toString(gameId),
                        Long.toString(playerId)}, null, null, null)) {
            if (!cursor.moveToFirst()) return 0;
            points = cursor.getInt(0);
        }
        ContentValues penalty = new ContentValues();
        penalty.put("game_id", gameId);
        penalty.put("player_id", playerId);
        penalty.put("points", -points);
        penalty.put("kind", kind);
        penalty.put("created_at", createdAt);
        penalty.put("source_entry_id", scoreId);
        db.insertOrThrow("score_entries", null, penalty);
        return points;
    }

    int currentPlayerPosition(long gameId, int playerCount) {
        if (playerCount <= 0) return 0;
        try (Cursor cursor = getReadableDatabase().query("games",
                new String[]{"current_player_position"}, "id=?",
                new String[]{Long.toString(gameId)}, null, null, null)) {
            if (cursor.moveToFirst()) return Math.floorMod(cursor.getInt(0), playerCount);
        }
        return 0;
    }

    void advanceTurn(long gameId, int currentPosition, int playerCount) {
        if (playerCount <= 0) return;
        ContentValues update = new ContentValues();
        update.put("current_player_position", (currentPosition + 1) % playerCount);
        update.put("updated_at", System.currentTimeMillis());
        getWritableDatabase().update("games", update, "id=?",
                new String[]{Long.toString(gameId)});
    }

    boolean deleteEntry(long entryId, long gameId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            if (isFinished(gameId)) {
                db.setTransactionSuccessful();
                return false;
            }
            try (Cursor cursor = db.query("score_entries",
                    new String[]{"kind", "player_id", "source_entry_id"},
                    "id=? AND game_id=?", new String[]{Long.toString(entryId), Long.toString(gameId)},
                    null, null, null)) {
                if (cursor.moveToFirst() && cursor.getString(0).equals("failure")) {
                    long playerId = cursor.getLong(1);
                    if (cursor.isNull(2)) {
                        adjustFailureMarks(db, "players", playerId, -1);
                    } else {
                        adjustFailureMarks(db, "score_entries", cursor.getLong(2), -1);
                    }
                }
            }
            db.delete("score_entries", "id=? AND game_id=?",
                    new String[]{Long.toString(entryId), Long.toString(gameId)});
            closePlayersAtOrBelowZero(db, gameId);
            long winnerId = winnerIdForGame(db, gameId);
            ContentValues update = new ContentValues();
            update.put("updated_at", System.currentTimeMillis());
            if (winnerId >= 0) {
                update.put("status", "finished");
                update.put("winner_id", winnerId);
            } else {
                update.put("status", "active");
                update.putNull("winner_id");
            }
            db.update("games", update, "id=?", new String[]{Long.toString(gameId)});
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    boolean deleteGame(long gameId) {
        return getWritableDatabase().delete("games", "id=?",
                new String[]{Long.toString(gameId)}) > 0;
    }

    private void adjustFailureMarks(SQLiteDatabase db, String table, long id, int delta) {
        int marks;
        try (Cursor cursor = db.query(table, new String[]{"failure_marks"}, "id=?",
                new String[]{Long.toString(id)}, null, null, null)) {
            if (!cursor.moveToFirst()) return;
            marks = cursor.getInt(0);
        }
        ContentValues update = new ContentValues();
        update.put("failure_marks", Math.max(0, marks + delta));
        db.update(table, update, "id=?", new String[]{Long.toString(id)});
    }

    void finishGame(long gameId, long winnerId) {
        ContentValues update = new ContentValues();
        update.put("status", "finished");
        update.put("winner_id", winnerId);
        update.put("updated_at", System.currentTimeMillis());
        getWritableDatabase().update("games", update, "id=?",
                new String[]{Long.toString(gameId)});
    }

    boolean isFinished(long gameId) {
        try (Cursor cursor = getReadableDatabase().query("games", new String[]{"status"},
                "id=?", new String[]{Long.toString(gameId)}, null, null, null)) {
            return cursor.moveToFirst() && cursor.getString(0).equals("finished");
        }
    }

    String playerName(long playerId) {
        return playerName(getReadableDatabase(), playerId);
    }

    private String playerName(SQLiteDatabase db, long playerId) {
        try (Cursor cursor = db.query("players", new String[]{"name"},
                "id=?", new String[]{Long.toString(playerId)}, null, null, null)) {
            return cursor.moveToFirst() ? cursor.getString(0) : "";
        }
    }

    static final class Game {
        final long id;
        final long createdAt;
        final String status;
        final long winnerId;
        final int currentPlayerPosition;

        Game(long id, long createdAt, String status, long winnerId, int currentPlayerPosition) {
            this.id = id;
            this.createdAt = createdAt;
            this.status = status;
            this.winnerId = winnerId;
            this.currentPlayerPosition = currentPlayerPosition;
        }
    }

    static final class Player {
        final long id;
        final String name;
        final int score;

        Player(long id, String name, int score) {
            this.id = id;
            this.name = name;
            this.score = score;
        }
    }

    static final class Entry {
        final long id;
        final String playerName;
        final int points;
        final String kind;
        final long createdAt;
        final int totalAfter;

        Entry(long id, String playerName, int points, String kind, long createdAt, int totalAfter) {
            this.id = id;
            this.playerName = playerName;
            this.points = points;
            this.kind = kind;
            this.createdAt = createdAt;
            this.totalAfter = totalAfter;
        }
    }

    static final class EntryResult {
        final boolean openingRejected;
        final List<Theft> thefts;
        final boolean failureThresholdReached;
        final boolean failurePenalty;
        final boolean exactGoalFailure;

        EntryResult(boolean openingRejected, List<Theft> thefts,
                    boolean failureThresholdReached, boolean failurePenalty,
                    boolean exactGoalFailure) {
            this.openingRejected = openingRejected;
            this.thefts = thefts;
            this.failureThresholdReached = failureThresholdReached;
            this.failurePenalty = failurePenalty;
            this.exactGoalFailure = exactGoalFailure;
        }
    }

    static final class Theft {
        final String playerWhoReachedTotal;
        final String playerWhoLostPoints;
        final int pointsRemoved;

        Theft(String playerWhoReachedTotal, String playerWhoLostPoints, int pointsRemoved) {
            this.playerWhoReachedTotal = playerWhoReachedTotal;
            this.playerWhoLostPoints = playerWhoLostPoints;
            this.pointsRemoved = pointsRemoved;
        }
    }

    static final class FailureResult {
        final boolean thresholdReached;
        final boolean penaltyApplied;
        final long palierId;

        FailureResult(boolean thresholdReached, boolean penaltyApplied, long palierId) {
            this.thresholdReached = thresholdReached;
            this.penaltyApplied = penaltyApplied;
            this.palierId = palierId;
        }
    }

    static final class Milestone {
        final int points;
        final int failureMarks;
        final boolean removed;
        final int totalAfter;

        Milestone(int points, int failureMarks, boolean removed, int totalAfter) {
            this.points = points;
            this.failureMarks = failureMarks;
            this.removed = removed;
            this.totalAfter = totalAfter;
        }
    }
}
