package fr.jeudes10000;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

final class GameDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "jeu_10000.db";
    private static final int DATABASE_VERSION = 2;
    private static final int TARGET_SCORE = 10000;

    GameDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE games (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, " +
                "status TEXT NOT NULL DEFAULT 'active', winner_id INTEGER)");
        db.execSQL("CREATE TABLE players (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "game_id INTEGER NOT NULL REFERENCES games(id) ON DELETE CASCADE, " +
                "name TEXT NOT NULL, position INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE score_entries (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "game_id INTEGER NOT NULL REFERENCES games(id) ON DELETE CASCADE, " +
                "player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE, " +
                "points INTEGER NOT NULL, kind TEXT NOT NULL, created_at INTEGER NOT NULL, " +
                "source_entry_id INTEGER REFERENCES score_entries(id) ON DELETE CASCADE)");
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
                new String[]{"id", "created_at", "status", "winner_id"},
                selection, args, null, null, "updated_at DESC", Integer.toString(limit))) {
            while (cursor.moveToNext()) {
                result.add(new Game(cursor.getLong(0), cursor.getLong(1),
                        cursor.getString(2), cursor.isNull(3) ? -1 : cursor.getLong(3)));
            }
        }
        return result;
    }

    List<Player> players(long gameId) {
        List<Player> result = new ArrayList<>();
        String sql = "SELECT p.id, p.name, COALESCE(SUM(e.points), 0) " +
                "FROM players p LEFT JOIN score_entries e " +
                "ON e.player_id=p.id AND e.kind!='legacy_theft' " +
                "WHERE p.game_id=? GROUP BY p.id ORDER BY p.position";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{Long.toString(gameId)})) {
            while (cursor.moveToNext()) {
                result.add(new Player(cursor.getLong(0), cursor.getString(1), cursor.getInt(2)));
            }
        }
        return result;
    }

    List<Entry> entries(long gameId) {
        List<Entry> result = new ArrayList<>();
        String sql = "SELECT e.id, p.name, e.points, e.kind, e.created_at " +
                "FROM score_entries e JOIN players p ON p.id=e.player_id " +
                "WHERE e.game_id=? ORDER BY e.created_at DESC, e.id DESC LIMIT 50";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{Long.toString(gameId)})) {
            while (cursor.moveToNext()) {
                result.add(new Entry(cursor.getLong(0), cursor.getString(1),
                        cursor.getInt(2), cursor.getString(3), cursor.getLong(4)));
            }
        }
        return result;
    }

    EntryResult addEntry(long gameId, long playerId, int points, String kind) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        ContentValues entry = new ContentValues();
        entry.put("game_id", gameId);
        entry.put("player_id", playerId);
        entry.put("points", points);
        entry.put("kind", kind);
        entry.put("created_at", now);
        db.beginTransaction();
        int tiedPlayers = 0;
        boolean thirdFailure = false;
        boolean failurePenalty = false;
        try {
            db.insertOrThrow("score_entries", null, entry);
            if (kind.equals("score") && points > 0) {
                String tieSql = "SELECT p.id FROM players p LEFT JOIN score_entries e " +
                        "ON e.player_id=p.id AND e.kind!='legacy_theft' " +
                        "WHERE p.game_id=? AND p.id!=? GROUP BY p.id " +
                        "HAVING COALESCE(SUM(e.points), 0)=" +
                        "(SELECT COALESCE(SUM(points), 0) FROM score_entries " +
                        "WHERE player_id=? AND kind!='legacy_theft') ORDER BY p.position";
                List<Long> tiedPlayerIds = new ArrayList<>();
                try (Cursor cursor = db.rawQuery(tieSql, new String[]{
                        Long.toString(gameId), Long.toString(playerId), Long.toString(playerId)})) {
                    while (cursor.moveToNext()) tiedPlayerIds.add(cursor.getLong(0));
                }
                tiedPlayers = tiedPlayerIds.size();
                for (long tiedPlayerId : tiedPlayerIds) {
                    addPenaltyForLastScore(db, gameId, tiedPlayerId, "theft", now);
                }
            } else if (kind.equals("failure")) {
                thirdFailure = consecutiveFailures(db, gameId, playerId) % 3 == 0;
                if (thirdFailure) {
                    failurePenalty = addPenaltyForLastScore(
                            db, gameId, playerId, "three_failures", now);
                }
            }
            ContentValues update = new ContentValues();
            update.put("updated_at", now);
            db.update("games", update, "id=?", new String[]{Long.toString(gameId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return new EntryResult(tiedPlayers, thirdFailure, failurePenalty);
    }

    private boolean addPenaltyForLastScore(SQLiteDatabase db, long gameId, long playerId,
                                           String kind, long createdAt) {
        String scoreSql = "SELECT e.id, e.points FROM score_entries e " +
                "WHERE e.game_id=? AND e.player_id=? AND e.kind='score' AND e.points>0 " +
                "AND NOT EXISTS (SELECT 1 FROM score_entries penalty " +
                "WHERE penalty.source_entry_id=e.id) " +
                "ORDER BY e.created_at DESC, e.id DESC LIMIT 1";
        long sourceEntryId;
        int score;
        try (Cursor cursor = db.rawQuery(scoreSql,
                new String[]{Long.toString(gameId), Long.toString(playerId)})) {
            if (!cursor.moveToFirst()) return false;
            sourceEntryId = cursor.getLong(0);
            score = cursor.getInt(1);
        }
        ContentValues penalty = new ContentValues();
        penalty.put("game_id", gameId);
        penalty.put("player_id", playerId);
        penalty.put("points", -score);
        penalty.put("kind", kind);
        penalty.put("created_at", createdAt);
        penalty.put("source_entry_id", sourceEntryId);
        db.insertOrThrow("score_entries", null, penalty);
        return true;
    }

    private int consecutiveFailures(SQLiteDatabase db, long gameId, long playerId) {
        int count = 0;
        String sql = "SELECT kind FROM score_entries WHERE game_id=? AND player_id=? " +
                "AND kind IN ('score', 'failure') ORDER BY id DESC";
        try (Cursor cursor = db.rawQuery(sql,
                new String[]{Long.toString(gameId), Long.toString(playerId)})) {
            while (cursor.moveToNext()) {
                if (!cursor.getString(0).equals("failure")) break;
                count++;
            }
        }
        return count;
    }

    void deleteEntry(long entryId, long gameId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("score_entries", "id=? AND game_id=?",
                    new String[]{Long.toString(entryId), Long.toString(gameId)});
            long winnerId = -1;
            String winnerSql = "SELECT p.id FROM players p LEFT JOIN score_entries e " +
                    "ON e.player_id=p.id AND e.kind!='legacy_theft' " +
                    "WHERE p.game_id=? GROUP BY p.id " +
                    "HAVING COALESCE(SUM(e.points), 0)>=? ORDER BY p.position LIMIT 1";
            try (Cursor cursor = db.rawQuery(winnerSql,
                    new String[]{Long.toString(gameId), Integer.toString(TARGET_SCORE)})) {
                if (cursor.moveToFirst()) winnerId = cursor.getLong(0);
            }
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
        } finally {
            db.endTransaction();
        }
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
        try (Cursor cursor = getReadableDatabase().query("players", new String[]{"name"},
                "id=?", new String[]{Long.toString(playerId)}, null, null, null)) {
            return cursor.moveToFirst() ? cursor.getString(0) : "";
        }
    }

    static final class Game {
        final long id;
        final long createdAt;
        final String status;
        final long winnerId;

        Game(long id, long createdAt, String status, long winnerId) {
            this.id = id;
            this.createdAt = createdAt;
            this.status = status;
            this.winnerId = winnerId;
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

        Entry(long id, String playerName, int points, String kind, long createdAt) {
            this.id = id;
            this.playerName = playerName;
            this.points = points;
            this.kind = kind;
            this.createdAt = createdAt;
        }
    }

    static final class EntryResult {
        final int tiedPlayers;
        final boolean thirdFailure;
        final boolean failurePenalty;

        EntryResult(int tiedPlayers, boolean thirdFailure, boolean failurePenalty) {
            this.tiedPlayers = tiedPlayers;
            this.thirdFailure = thirdFailure;
            this.failurePenalty = failurePenalty;
        }
    }
}
