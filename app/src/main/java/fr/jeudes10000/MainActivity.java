package fr.jeudes10000;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MainActivity extends Activity {
    private static final int TARGET_SCORE = 10000;
    private static final int INK = 0xFF18312C;
    private static final int MUTED = 0xFF64756F;
    private static final int GREEN = 0xFF167D68;
    private static final int BACKGROUND = 0xFFF5F7F6;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int LINE = 0xFFE2E9E6;

    private static final Map<String, String[]> TEXT = new HashMap<>();
    static {
        TEXT.put("home", new String[]{"Jeu des 10 000", "10,000 Dice Game", "Juego de los 10 000"});
        TEXT.put("tagline", new String[]{"Une partie, tous les scores, sans rien perdre.", "One game, every score, nothing lost.", "Una partida, todos los puntos, sin perder nada."});
        TEXT.put("resume", new String[]{"Reprendre la partie", "Resume game", "Continuar partida"});
        TEXT.put("no_active", new String[]{"Aucune partie en cours", "No game in progress", "No hay partidas en curso"});
        TEXT.put("new_game", new String[]{"Nouvelle partie", "New game", "Nueva partida"});
        TEXT.put("recent", new String[]{"Dernières parties", "Recent games", "Partidas recientes"});
        TEXT.put("rules", new String[]{"Règles du jeu", "Game rules", "Reglas del juego"});
        TEXT.put("language", new String[]{"Langue", "Language", "Idioma"});
        TEXT.put("players", new String[]{"Joueurs", "Players", "Jugadores"});
        TEXT.put("choose_players", new String[]{"Choisissez le nombre de joueurs et leurs noms.", "Choose the number of players and their names.", "Elige el número de jugadores y sus nombres."});
        TEXT.put("start", new String[]{"Commencer la partie", "Start game", "Empezar partida"});
        TEXT.put("back", new String[]{"Retour", "Back", "Volver"});
        TEXT.put("scoreboard", new String[]{"Scores", "Scoreboard", "Marcador"});
        TEXT.put("target", new String[]{"Victoire à exactement 10 000 points", "Win at exactly 10,000 points", "Victoria con exactamente 10 000 puntos"});
        TEXT.put("total", new String[]{"Total", "Total", "Total"});
        TEXT.put("record", new String[]{"Inscrire un score", "Record a score", "Anotar puntos"});
        TEXT.put("current_turn", new String[]{"À vous de jouer : ", "Current player: ", "Turno de: "});
        TEXT.put("paliers", new String[]{"Paliers récents", "Recent score tiers", "Últimos niveles"});
        TEXT.put("details", new String[]{"Détail", "Details", "Detalle"});
        TEXT.put("detail_title", new String[]{"Paliers de ", "Score tiers for ", "Niveles de "});
        TEXT.put("tap_details", new String[]{"Touchez un joueur pour voir ses paliers.", "Tap a player to view their score tiers.", "Toca un jugador para ver sus niveles."});
        TEXT.put("no_paliers", new String[]{"Aucun palier. Il faut marquer 600 points en un tour pour entrer ou rouvrir le score.", "No score tier yet. Score 600 points in one turn to enter or reopen.", "Aún no hay nivel. Anota 600 puntos en un turno para entrar o volver a abrir."});
        TEXT.put("palier", new String[]{"Palier", "Tier", "Nivel"});
        TEXT.put("removed", new String[]{"retiré", "removed", "retirado"});
        TEXT.put("need_600", new String[]{"Il faut au moins 600 points en un seul tour pour entrer ou rouvrir le score. Score ignoré.", "At least 600 points in one turn are required to enter or reopen. Score ignored.", "Se necesitan al menos 600 puntos en un turno para entrar o volver a abrir. Puntuación ignorada."});
        TEXT.put("exact_goal_failure", new String[]{"Il fallait atteindre exactement 10 000 points. Score refusé, tour compté comme un échec.", "You must reach exactly 10,000 points. The score was rejected and the turn counts as a failure.", "Hay que llegar exactamente a 10 000 puntos. Puntuación rechazada; el turno cuenta como fallo."});
        TEXT.put("select_player", new String[]{"Choisir un joueur", "Choose a player", "Elegir jugador"});
        TEXT.put("points_hint", new String[]{"Points (négatif pour une correction)", "Points (negative to correct)", "Puntos (negativos para corregir)"});
        TEXT.put("save_score", new String[]{"Valider", "Save", "Guardar"});
        TEXT.put("failure", new String[]{"Échec : tour perdu", "Farkle: turn lost", "Fallo: turno perdido"});
        TEXT.put("evolution", new String[]{"Évolution des scores", "Score history", "Evolución de puntos"});
        TEXT.put("nothing_yet", new String[]{"Aucune saisie pour le moment.", "No scores recorded yet.", "Todavía no hay puntuaciones."});
        TEXT.put("undo", new String[]{"Annuler", "Undo", "Deshacer"});
        TEXT.put("confirm_undo", new String[]{"Supprimer cette saisie ? Le total sera recalculé.", "Delete this entry? The total will be recalculated.", "¿Eliminar esta anotación? Se recalculará el total."});
        TEXT.put("cancel", new String[]{"Garder", "Keep", "Conservar"});
        TEXT.put("confirm", new String[]{"Supprimer", "Delete", "Eliminar"});
        TEXT.put("no_players", new String[]{"Ajoutez au moins 2 joueurs.", "Add at least 2 players.", "Añade al menos 2 jugadores."});
        TEXT.put("enter_points", new String[]{"Saisissez un nombre de points différent de zéro.", "Enter a non-zero number of points.", "Introduce una cantidad de puntos distinta de cero."});
        TEXT.put("wins", new String[]{"Victoire de", "Winner:", "Ganador:"});
        TEXT.put("finished", new String[]{"Partie terminée", "Game finished", "Partida terminada"});
        TEXT.put("ongoing", new String[]{"En cours", "In progress", "En curso"});
        TEXT.put("no_history", new String[]{"Aucune partie enregistrée.", "No games recorded.", "No hay partidas guardadas."});
        TEXT.put("game", new String[]{"Partie", "Game", "Partida"});
        TEXT.put("failure_event", new String[]{"Échec", "Farkle", "Fallo"});
        TEXT.put("correction", new String[]{"Correction", "Correction", "Corrección"});
        TEXT.put("points", new String[]{"points", "points", "puntos"});
        TEXT.put("theft", new String[]{"Vol de points", "Points stolen", "Robo de puntos"});
        TEXT.put("opening_ignored", new String[]{"Ouverture non atteinte (score ignoré)", "Opening threshold missed (score ignored)", "No alcanzó el mínimo de apertura (puntuación ignorada)"});
        TEXT.put("legacy_theft", new String[]{"Ancien score annulé", "Previous score voided", "Puntuación anterior anulada"});
        TEXT.put("three_failures", new String[]{"Perte après 3 échecs", "Loss after 3 failures", "Pérdida tras 3 fallos"});
        TEXT.put("rules_intro", new String[]{"Une variante courante : convenez des règles avant de commencer, car elles varient selon les tables.", "A common variant: agree on the rules before playing, as they vary between tables.", "Una variante habitual: acordad las reglas antes de jugar, ya que varían según la mesa."});
        TEXT.put("rules_body", new String[]{
                "But : atteindre exactement 10 000 points pour gagner. Les points des combinaisons sont saisis manuellement. Dépasser 10 000 annule les points du tour et compte comme un échec.\n\n• Pour entrer dans la partie, il faut marquer au moins 600 points en un seul tour ; en dessous, les points sont ignorés sans compter comme échec. Si le total retombe à 0 ou moins, il faut refaire 600 points en un tour pour rouvrir le score.\n• Chaque 1 vaut 100 points ; chaque 5 vaut 50 points.\n• Brelan : trois 1 valent 1 000 ; trois dés identiques de 2 à 6 valent face x 100.\n• Quatre, cinq ou six dés identiques valent 2x, 3x ou 4x la valeur du brelan.\n• Suite de 1 à 6 : 1 500 points. Trois paires : 1 500 points.\n• Échec : aucun point marqué arrête le tour et rapporte 0. Les échecs sont attachés au palier courant ; au troisième, le dernier gain est retiré et les croix du palier précédent sont conservées.\n• Égalité : si un joueur rejoint exactement le total d'un ou plusieurs adversaires, chacun perd les points de son dernier tour réussi. Les nouvelles égalités déclenchent d'autres pertes en cascade ; un même gain n'est retiré qu'une fois.\n\nLes variantes (combinaisons, fin de partie) changent selon les joueurs. Cette application suit les scores, elle ne lance pas les dés.",
                "Goal: win by reaching exactly 10,000 points. Enter the points for each scoring combination manually. Going over 10,000 cancels the turn's points and counts as a failure.\n\n• To enter the game, a player must score at least 600 points in one turn; lower scores are ignored and do not count as failures. If the total falls to 0 or below, they must score 600 in one turn again to reopen their score.\n• Each 1 is worth 100 points; each 5 is worth 50.\n• Three of a kind: three 1s are worth 1,000; three matching dice from 2 to 6 are worth face value x 100.\n• Four, five, or six matching dice are worth 2x, 3x, or 4x the three-of-a-kind score.\n• Straight from 1 to 6: 1,500 points. Three pairs: 1,500 points.\n• Farkle: scoring no combination ends the turn and scores 0. Failures belong to the current tier; on the third, its latest gain is removed and the previous tier's crosses are kept.\n• Tie: if a player reaches exactly the same total as one or more opponents, each loses the points from their latest successful turn. New ties trigger further losses in a cascade; each gain can be removed only once.\n\nVariations (combinations, game end) differ between groups. This app tracks scores; it does not roll dice.",
                "Objetivo: ganar al llegar exactamente a 10 000 puntos. Anota manualmente los puntos de cada combinación. Superar los 10 000 anula los puntos del turno y cuenta como fallo.\n\n• Para entrar en la partida, hay que anotar al menos 600 puntos en un solo turno; las puntuaciones menores se ignoran y no cuentan como fallos. Si el total vuelve a 0 o menos, hay que volver a anotar 600 puntos en un turno para reabrir el marcador.\n• Cada 1 vale 100 puntos; cada 5 vale 50.\n• Trío: tres 1 valen 1 000; tres dados iguales del 2 al 6 valen cara x 100.\n• Cuatro, cinco o seis dados iguales valen 2x, 3x o 4x el valor del trío.\n• Escalera del 1 al 6: 1 500 puntos. Tres parejas: 1 500 puntos.\n• Fallo: no conseguir una combinación termina el turno y suma 0. Los fallos pertenecen al nivel actual; al tercero se quitan sus últimos puntos ganados y se conservan las cruces del nivel anterior.\n• Empate: si un jugador alcanza exactamente el mismo total que uno o varios rivales, cada uno pierde los puntos de su último turno exitoso. Los nuevos empates generan pérdidas en cascada; cada ganancia solo puede quitarse una vez.\n\nLas variantes (combinaciones y final) dependen del grupo. Esta aplicación registra puntos, no lanza dados."
        });
        TEXT.put("language_names", new String[]{"Français", "English", "Español"});
        TEXT.put("player_default", new String[]{"Joueur ", "Player ", "Jugador "});
        TEXT.put("entry_saved", new String[]{"Score enregistré.", "Score saved.", "Puntuación guardada."});
        TEXT.put("tie_message", new String[]{
                "Égalité : chaque adversaire à ce total perd les points de son dernier tour réussi.",
                "Tie: each opponent at that total loses the points from their latest successful turn.",
                "Empate: cada rival con ese total pierde los puntos de su último turno exitoso."
        });
        TEXT.put("three_failures_message", new String[]{
                "Trois échecs sur ce palier : son gain est retiré.",
                "Three failures on this tier: its gain is removed.",
                "Tres fallos en este nivel: se quitan sus puntos."
        });
        TEXT.put("cascade_message", new String[]{
                "Les pertes ont été appliquées en cascade.",
                "The score losses were applied in a cascade.",
                "Las pérdidas se aplicaron en cascada."
        });
        TEXT.put("no_score_to_remove", new String[]{
                "Trois échecs consécutifs, mais aucun gain précédent à retirer.",
                "Three consecutive failures, but there is no previous score to remove.",
                "Tres fallos consecutivos, pero no hay puntos anteriores que quitar."
        });
        TEXT.put("done", new String[]{"Terminer", "Finish", "Terminar"});
    }

    private GameDatabase database;
    private int language;
    private EditText[] playerNameFields;
    private NumberPicker playerCountPicker;
    private long currentGameId = -1;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BACKGROUND);
        getWindow().setNavigationBarColor(BACKGROUND);
        getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        database = new GameDatabase(this);
        language = getPreferences(MODE_PRIVATE).getInt("language", 0);
        if (state != null) {
            language = state.getInt("language", language);
            currentGameId = state.getLong("game_id", -1);
        }
        showHome();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        out.putInt("language", language);
        out.putLong("game_id", currentGameId);
        super.onSaveInstanceState(out);
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }

    private String t(String key) {
        String[] values = TEXT.get(key);
        return values == null ? key : values[language];
    }

    private void setScreen(LinearLayout content) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);
        if (Build.VERSION.SDK_INT >= 35) {
            scroll.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets bars = insets.getInsets(
                        android.view.WindowInsets.Type.systemBars()
                                | android.view.WindowInsets.Type.displayCutout());
                view.setPadding(0, bars.top, 0, bars.bottom);
                return insets;
            });
        }
        scroll.addView(content);
        setContentView(scroll);
        if (Build.VERSION.SDK_INT >= 35) scroll.requestApplyInsets();
    }

    private LinearLayout page() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(24), dp(24), dp(32));
        layout.setBackgroundColor(BACKGROUND);
        return layout;
    }

    private void heading(LinearLayout layout, String title, String subtitle) {
        TextView titleView = text(title, 30, INK, true);
        titleView.setPadding(0, dp(12), 0, dp(6));
        layout.addView(titleView);
        if (subtitle != null) {
            TextView subtitleView = text(subtitle, 15, MUTED, false);
            subtitleView.setPadding(0, 0, 0, dp(18));
            layout.addView(subtitleView);
        }
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private void section(LinearLayout layout, String value) {
        TextView view = text(value, 19, INK, true);
        view.setPadding(0, dp(22), 0, dp(10));
        layout.addView(view);
    }

    private void button(LinearLayout layout, String label, boolean primary, Runnable action) {
        TextView view = text(label, 16, primary ? WHITE : GREEN, true);
        view.setGravity(Gravity.CENTER);
        view.setMinHeight(dp(52));
        view.setPadding(dp(16), dp(12), dp(16), dp(12));
        view.setBackground(round(primary ? GREEN : WHITE, 14, primary ? GREEN : LINE));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(7), 0, dp(7));
        layout.addView(view, params);
        view.setOnClickListener(v -> action.run());
    }

    private TextView compactButton(String label, boolean primary) {
        TextView view = text(label, 15, primary ? WHITE : GREEN, true);
        view.setGravity(Gravity.CENTER);
        view.setMinHeight(dp(48));
        view.setPadding(dp(16), dp(10), dp(16), dp(10));
        view.setBackground(round(primary ? GREEN : WHITE, 12, primary ? GREEN : LINE));
        return view;
    }

    private android.graphics.drawable.GradientDrawable round(int color, int radius, int stroke) {
        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private EditText input(String hint, int inputType) {
        EditText edit = new EditText(this);
        edit.setSingleLine(true);
        edit.setTextSize(16);
        edit.setTextColor(INK);
        edit.setHintTextColor(MUTED);
        edit.setHint(hint);
        edit.setInputType(inputType);
        edit.setPadding(dp(14), dp(10), dp(14), dp(10));
        edit.setBackground(round(WHITE, 12, LINE));
        return edit;
    }

    private void showHome() {
        currentGameId = -1;
        LinearLayout layout = page();
        heading(layout, t("home"), t("tagline"));
        section(layout, t("language"));
        Spinner languageSpinner = new Spinner(this);
        languageSpinner.setAdapter(adapter(Arrays.asList(TEXT.get("language_names"))));
        languageSpinner.setSelection(language);
        layout.addView(languageSpinner);
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean initialized;
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!initialized) { initialized = true; return; }
                language = position;
                getPreferences(MODE_PRIVATE).edit().putInt("language", language).apply();
                showHome();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        List<GameDatabase.Game> active = database.games("active", 1);
        section(layout, t("resume"));
        if (active.isEmpty()) {
            TextView empty = text(t("no_active"), 15, MUTED, false);
            empty.setPadding(0, dp(8), 0, dp(8));
            layout.addView(empty);
        } else {
            GameDatabase.Game game = active.get(0);
            button(layout, t("resume"), true, () -> showGame(game.id));
        }
        button(layout, t("new_game"), active.isEmpty(), this::showSetup);
        button(layout, t("recent"), false, this::showHistory);
        button(layout, t("rules"), false, this::showRules);
        setScreen(layout);
    }

    private void showPlayerDetails(long gameId, GameDatabase.Player player) {
        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(dp(20), dp(8), dp(20), dp(12));
        List<GameDatabase.Milestone> milestones = database.milestones(gameId, player.id);
        if (milestones.isEmpty()) {
            details.addView(text(t("no_paliers"), 14, MUTED, false));
        } else {
            for (GameDatabase.Milestone milestone : milestones) {
                String line = t("palier") + "  +" + formatScore(milestone.points) + " " +
                        t("points") + "  ·  " + t("total") + ": " +
                        formatScore(milestone.totalAfter) +
                        crosses(milestone.failureMarks) +
                        (milestone.removed ? " · " + t("removed") : "");
                TextView tier = text(line, 14, milestone.removed ? MUTED : INK, false);
                tier.setPadding(0, dp(8), 0, dp(8));
                details.addView(tier);
                View divider = new View(this);
                divider.setBackgroundColor(LINE);
                details.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
            }
        }
        ScrollView scroll = new ScrollView(this);
        scroll.addView(details);
        new AlertDialog.Builder(this)
                .setTitle(t("detail_title") + player.name)
                .setMessage(t("total") + ": " + formatScore(player.score))
                .setView(scroll)
                .setPositiveButton("OK", null)
                .show();
    }

    private String formatScore(int score) {
        return String.format(locale(), "%,d", score);
    }

    private void showSetup() {
        LinearLayout layout = page();
        heading(layout, t("new_game"), t("choose_players"));
        section(layout, t("players"));
        playerCountPicker = new NumberPicker(this);
        playerCountPicker.setMinValue(2);
        playerCountPicker.setMaxValue(8);
        playerCountPicker.setWrapSelectorWheel(false);
        playerCountPicker.setValue(2);
        layout.addView(playerCountPicker, new LinearLayout.LayoutParams(-1, dp(110)));
        LinearLayout fields = new LinearLayout(this);
        fields.setOrientation(LinearLayout.VERTICAL);
        layout.addView(fields);
        playerNameFields = new EditText[2];
        rebuildNameFields(fields, 2, null);
        playerCountPicker.setOnValueChangedListener((picker, oldValue, newValue) ->
                rebuildNameFields(fields, newValue, playerNameFields));
        button(layout, t("start"), true, () -> {
            List<String> names = new ArrayList<>();
            for (int i = 0; i < playerNameFields.length; i++) {
                String name = playerNameFields[i].getText().toString().trim();
                if (name.isEmpty()) name = t("player_default") + (i + 1);
                names.add(name);
            }
            if (names.size() < 2) {
                toast(t("no_players"));
                return;
            }
            showGame(database.createGame(names));
        });
        button(layout, t("back"), false, this::showHome);
        setScreen(layout);
    }

    private void rebuildNameFields(LinearLayout container, int count, EditText[] previous) {
        String[] names = new String[count];
        if (previous != null) {
            for (int i = 0; i < Math.min(previous.length, count); i++) {
                names[i] = previous[i].getText().toString();
            }
        }
        container.removeAllViews();
        playerNameFields = new EditText[count];
        for (int i = 0; i < count; i++) {
            EditText field = input(t("player_default") + (i + 1),
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            if (names[i] != null) field.setText(names[i]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.setMargins(0, dp(5), 0, dp(5));
            container.addView(field, params);
            playerNameFields[i] = field;
        }
    }

    private void showGame(long gameId) {
        currentGameId = gameId;
        if (database.resolveExistingTies(gameId) > 0) {
            toast(t("tie_message") + " " + t("cascade_message"));
        }
        List<GameDatabase.Player> players = database.players(gameId);
        if (players.isEmpty()) {
            showHome();
            return;
        }
        LinearLayout layout = page();
        layout.setPadding(dp(16), dp(8), dp(16), dp(20));
        int currentPosition = database.currentPlayerPosition(gameId, players.size());
        GameDatabase.Player currentPlayer = players.get(currentPosition);
        TextView currentTurn = text(t("current_turn") + currentPlayer.name, 21, GREEN, true);
        currentTurn.setPadding(dp(14), dp(12), dp(14), dp(12));
        currentTurn.setBackground(round(0xFFEAF4F0, 12, 0xFFD4E7DF));
        layout.addView(currentTurn);
        TextView target = text(t("target"), 13, MUTED, false);
        target.setPadding(dp(14), dp(4), 0, dp(4));
        layout.addView(target);

        if (database.isFinished(gameId)) {
            TextView finished = text(t("finished"), 15, GREEN, true);
            finished.setPadding(0, dp(18), 0, dp(8));
            layout.addView(finished);
        } else {
            TextView recordHeading = text(t("record"), 17, INK, true);
            recordHeading.setPadding(0, dp(8), 0, dp(2));
            layout.addView(recordHeading);
            Spinner playerSpinner = new Spinner(this);
            List<String> playerNames = new ArrayList<>();
            for (GameDatabase.Player player : players) playerNames.add(player.name);
            playerSpinner.setAdapter(adapter(playerNames));
            playerSpinner.setSelection(currentPosition);
            layout.addView(playerSpinner);
            EditText points = input(t("points_hint"),
                    InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            points.setImeOptions(EditorInfo.IME_ACTION_DONE);
            LinearLayout inputRow = new LinearLayout(this);
            inputRow.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams pointsParams = new LinearLayout.LayoutParams(0, -2, 1);
            pointsParams.setMargins(0, dp(4), dp(8), dp(4));
            inputRow.addView(points, pointsParams);
            TextView save = compactButton(t("save_score"), true);
            inputRow.addView(save);
            layout.addView(inputRow);
            Runnable saveScore = () -> {
                String value = points.getText().toString().trim();
                try {
                    int amount = Integer.parseInt(value);
                    if (amount == 0) {
                        toast(t("enter_points"));
                        return;
                    }
                    GameDatabase.Player selected = players.get(playerSpinner.getSelectedItemPosition());
                    int playerPosition = playerSpinner.getSelectedItemPosition();
                    GameDatabase.EntryResult result = database.addEntry(gameId, selected.id, amount,
                            amount < 0 ? "correction" : "score");
                    points.setText("");
                    if (result.exactGoalFailure) {
                        String message = t("exact_goal_failure");
                        if (result.failureThresholdReached) {
                            message += " " + (result.failurePenalty ? t("three_failures_message") :
                                    t("no_score_to_remove"));
                        }
                        if (result.tiedPlayers > 0) message += " " + t("cascade_message");
                        toast(message);
                    } else if (result.openingRejected) {
                        toast(t("need_600"));
                    } else if (result.tiedPlayers > 0) {
                        toast(t("tie_message") + " " + t("cascade_message"));
                    } else {
                        toast(t("entry_saved"));
                    }
                    if (amount > 0) {
                        database.advanceTurn(gameId, playerPosition, players.size());
                    }
                    checkWinner(gameId);
                    showGame(gameId);
                } catch (NumberFormatException exception) {
                    toast(t("enter_points"));
                }
            };
            save.setOnClickListener(v -> saveScore.run());
            points.setOnEditorActionListener((view, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveScore.run();
                    return true;
                }
                return false;
            });
            button(layout, t("failure"), false, () -> {
                int playerPosition = playerSpinner.getSelectedItemPosition();
                GameDatabase.Player selected = players.get(playerPosition);
                GameDatabase.EntryResult result = database.addEntry(gameId, selected.id, 0, "failure");
                database.advanceTurn(gameId, playerPosition, players.size());
                if (result.tiedPlayers > 0) {
                    toast(t("cascade_message"));
                } else if (!result.failureThresholdReached) {
                    toast(t("entry_saved"));
                } else {
                    toast(result.failurePenalty ? t("three_failures_message") :
                            t("no_score_to_remove"));
                }
                showGame(gameId);
            });
        }

        section(layout, t("scoreboard"));
        TextView detailsHint = text(t("tap_details"), 12, MUTED, false);
        detailsHint.setPadding(0, 0, 0, dp(4));
        layout.addView(detailsHint);
        for (int i = 0; i < players.size(); i++) {
            GameDatabase.Player player = players.get(i);
            LinearLayout card = new LinearLayout(this);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(14), dp(9), dp(12), dp(9));
            card.setBackground(round(i == currentPosition ? 0xFFEAF4F0 : WHITE,
                    12, i == currentPosition ? 0xFFD4E7DF : LINE));
            TextView name = text(player.name, 16, INK, true);
            TextView score = text(t("total") + ": " + formatScore(player.score),
                    15, GREEN, true);
            String crossMarks = crosses(Math.min(2,
                    database.currentFailureMarks(gameId, player.id)));
            TextView details = text(t("details"), 12, MUTED, true);
            card.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
            card.addView(score);
            if (!crossMarks.isEmpty()) {
                TextView spacedCrosses = text("  " + crossMarks, 14, 0xFFB54747, true);
                card.addView(spacedCrosses);
            }
            LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(-2, -2);
            detailsParams.setMargins(dp(10), 0, 0, 0);
            card.addView(details, detailsParams);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
            cardParams.setMargins(0, dp(3), 0, dp(3));
            layout.addView(card, cardParams);
            card.setFocusable(true);
            card.setContentDescription(t("detail_title") + player.name);
            card.setOnClickListener(v -> showPlayerDetails(gameId, player));
        }

        section(layout, t("evolution"));
        List<GameDatabase.Entry> entries = database.entries(gameId);
        if (entries.isEmpty()) {
            layout.addView(text(t("nothing_yet"), 14, MUTED, false));
        }
        for (GameDatabase.Entry entry : entries) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, dp(10), 0, dp(10));
            TextView detail = text(entry.playerName + "  ·  " + entryLabel(entry) +
                    (entry.points == 0 ? "" : "  " + (entry.points > 0 ? "+" : "") + entry.points +
                            " " + t("points")) + "  ·  " + t("total") + ": " +
                    formatScore(entry.totalAfter), 15, INK, false);
            row.addView(detail);
            TextView date = text(DateFormat.getDateTimeInstance(DateFormat.SHORT,
                    DateFormat.SHORT, locale()).format(new Date(entry.createdAt)), 12, MUTED, false);
            date.setPadding(0, dp(3), 0, 0);
            row.addView(date);
            layout.addView(row);
            View divider = new View(this);
            divider.setBackgroundColor(LINE);
            layout.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
            TextView undo = text(t("undo"), 14, GREEN, true);
            undo.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            undo.setPadding(0, dp(8), 0, dp(4));
            layout.addView(undo);
            undo.setOnClickListener(v -> confirmDelete(entry.id, gameId));
        }
        button(layout, t("back"), false, this::showHome);
        setScreen(layout);
    }

    private String entryLabel(GameDatabase.Entry entry) {
        if (entry.kind.equals("failure")) return t("failure_event");
        if (entry.kind.equals("correction")) return t("correction");
        if (entry.kind.equals("theft")) return t("theft");
        if (entry.kind.equals("three_failures")) return t("three_failures");
        if (entry.kind.equals("legacy_theft")) return t("legacy_theft");
        if (entry.kind.equals("opening_ignored")) return t("opening_ignored");
        return t("scoreboard");
    }

    private String crosses(int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) result.append("  X");
        return result.toString();
    }

    private void checkWinner(long gameId) {
        List<GameDatabase.Player> players = database.players(gameId);
        for (GameDatabase.Player player : players) {
            if (player.score == TARGET_SCORE) {
                database.finishGame(gameId, player.id);
                new AlertDialog.Builder(this)
                        .setTitle(t("finished"))
                        .setMessage(t("wins") + " " + player.name + "!")
                        .setPositiveButton("OK", (dialog, which) -> showGame(gameId))
                        .show();
                return;
            }
        }
    }

    private void confirmDelete(long entryId, long gameId) {
        new AlertDialog.Builder(this)
                .setMessage(t("confirm_undo"))
                .setNegativeButton(t("cancel"), null)
                .setPositiveButton(t("confirm"), (dialog, which) -> {
                    database.deleteEntry(entryId, gameId);
                    showGame(gameId);
                })
                .show();
    }

    private void showHistory() {
        LinearLayout layout = page();
        heading(layout, t("recent"), null);
        List<GameDatabase.Game> games = database.games(null, 30);
        if (games.isEmpty()) layout.addView(text(t("no_history"), 15, MUTED, false));
        for (GameDatabase.Game game : games) {
            String status = game.status.equals("finished") ? t("wins") + " " +
                    database.playerName(game.winnerId) : t("ongoing");
            String title = t("game") + " · " +
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale())
                            .format(new Date(game.createdAt));
            button(layout, title + "\n" + status, false, () -> showGame(game.id));
        }
        button(layout, t("back"), false, this::showHome);
        setScreen(layout);
    }

    private void showRules() {
        LinearLayout layout = page();
        heading(layout, t("rules"), t("rules_intro"));
        TextView body = text(t("rules_body"), 16, INK, false);
        body.setLineSpacing(dp(5), 1f);
        body.setPadding(dp(16), dp(16), dp(16), dp(16));
        body.setBackground(round(WHITE, 14, LINE));
        layout.addView(body);
        button(layout, t("back"), false, this::showHome);
        setScreen(layout);
    }

    private ArrayAdapter<String> adapter(List<String> values) {
        ArrayAdapter<String> result = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, values);
        result.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return result;
    }

    private Locale locale() {
        return language == 1 ? Locale.ENGLISH : language == 2 ? new Locale("es") : Locale.FRENCH;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
