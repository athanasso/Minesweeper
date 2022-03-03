package minesweeperapp;

import static java.lang.Math.round;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MinesweeperApp extends Application
{

    //images
    static Image bomb = new Image("bomb.png");
    static Image bombLarge = new Image("bombLarge.png");
    static Image flaggedBomb = new Image("flaggedBomb.png");
    static Image notBomb = new Image("notBomb.png");
    static Image victory = new Image("victory.png");
    static Image defeat = new Image("defeat.png");
    static Image clock = new Image("clock.png");
    static Image newGame = new Image("newGame.png");
    static Image fatalBomb = new Image("fatalBomb.png");

    protected static Stage primaryStage;
    protected static VBox root = new VBox();
    protected static int dimensions = 15, bombPercent = 15, clickedSquares, bombsFound, flagCount, bombAmount;
    protected static Square[][] field;
    protected static boolean isFirstClick = true, isPaused = true, isOver = false;

    private static int secondsPassed;
    public static Timer timer, printTimer;

    @Override
    public void start(Stage stage)
    {
        primaryStage = stage;
        field = new Square[dimensions][dimensions];

        MenuBar menuBar = new MenuBar();
        //1st menu, for the size of the play area
        Label labelSize = new Label("Size");
        Menu menuSize = new Menu("", labelSize);
        RadioMenuItem small = new RadioMenuItem("_Small");
        RadioMenuItem medium = new RadioMenuItem("_Medium");
        RadioMenuItem large = new RadioMenuItem("_Large");
        //the actions of the 1st menu
        labelSize.setOnMouseMoved(e -> menuSize.show());
        small.setOnAction(e ->
        {
            dimensions = 10;
            small.setDisable(true);
            medium.setDisable(false);
            large.setDisable(false);
            restart();
        });
        medium.setOnAction(e ->
        {
            dimensions = 15;
            medium.setDisable(true);
            small.setDisable(false);
            large.setDisable(false);
            restart();
        });
        large.setOnAction(e ->
        {
            dimensions = 20;
            large.setDisable(true);
            small.setDisable(false);
            medium.setDisable(false);
            restart();
        });
        ToggleGroup toggleSize = new ToggleGroup();
        toggleSize.getToggles().addAll(small, medium, large);
        toggleSize.selectToggle(medium);
        medium.setDisable(true);
        menuSize.getItems().addAll(small, new SeparatorMenuItem(), medium, new SeparatorMenuItem(), large);

        //2nd menu, for the difficulty of the game (percentage of bombs in field)
        Label labelDifficulty = new Label("Difficulty");
        Menu menuDifficulty = new Menu("", labelDifficulty);
        RadioMenuItem easy = new RadioMenuItem("_Easy");
        RadioMenuItem normal = new RadioMenuItem("_Normal");
        RadioMenuItem hard = new RadioMenuItem("_Hard");
        //the actions of the 2nd menu
        labelDifficulty.setOnMouseMoved(e -> menuDifficulty.show());
        easy.setOnAction(e ->
        {
            bombPercent = 10;//10%
            easy.setDisable(true);
            normal.setDisable(false);
            hard.setDisable(false);
            restart();
        });
        normal.setOnAction(e ->
        {
            bombPercent = 15;//15%
            normal.setDisable(true);
            easy.setDisable(false);
            hard.setDisable(false);
            restart();
        });
        hard.setOnAction(e ->
        {
            bombPercent = 20;//20%
            hard.setDisable(true);
            easy.setDisable(false);
            normal.setDisable(false);
            restart();
        });
        bombAmount = (int) (round((dimensions * dimensions) * (bombPercent / 100.0)));
        ToggleGroup toggleDifficulty = new ToggleGroup();
        toggleDifficulty.getToggles().addAll(easy, normal, hard);
        toggleDifficulty.selectToggle(normal);
        normal.setDisable(true);
        menuDifficulty.getItems().addAll(easy, new SeparatorMenuItem(), normal, new SeparatorMenuItem(), hard);
        menuBar.getMenus().addAll(menuSize, menuDifficulty);

        BorderPane playerBar = new BorderPane();
        BorderPane centerBar = new BorderPane();
        playerBar.setCenter(centerBar);

        Label clockLabel = new Label();
        clockLabel.setAlignment(Pos.CENTER);
        clockLabel.setId("playerBarLabel");
        clockLabel.setMinSize(100, 60);
        clockLabel.setMaxSize(100, 60);
        clockLabel.setPrefSize(100, 60);
        playerBar.setLeft(clockLabel);

        Button newGameButton = new Button("", new ImageView(newGame));
        newGameButton.setId("newGame");
        newGameButton.setMinSize(100, 60);
        newGameButton.setMaxSize(100, 60);
        newGameButton.setPrefSize(100, 60);
        newGameButton.setOnAction(e -> restart());
        ImageView clockView = new ImageView(clock);
        centerBar.setLeft(clockView);
        centerBar.setAlignment(clockView, Pos.CENTER);
        BorderPane.setMargin(clockView, new Insets(0, 10, 0, 10));
        centerBar.setCenter(newGameButton);
        ImageView bombView = new ImageView(bombLarge);
        centerBar.setRight(bombView);
        centerBar.setAlignment(bombView, Pos.CENTER);
        BorderPane.setMargin(bombView, new Insets(0, 10, 0, 10));
        Label bombLabel = new Label("34");
        bombLabel.setAlignment(Pos.CENTER);
        bombLabel.setId("playerBarLabel");
        bombLabel.setMinSize(100, 60);
        bombLabel.setMaxSize(100, 60);
        bombLabel.setPrefSize(100, 60);
        playerBar.setRight(bombLabel);

        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                if (!(isFirstClick))
                {
                    secondsPassed++;
                }
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 1000);

        TimerTask printTask = new TimerTask()
        {
            @Override
            public void run()
            {
                if (!isOver)
                {
                    if (isPaused)
                    {
                        secondsPassed = 0;
                        Platform.runLater(() -> clockLabel.setText("0"));
                    } else
                    {
                        Platform.runLater(() -> clockLabel.setText(Integer.toString(secondsPassed)));
                    }
                }
            }
        };
        printTimer = new Timer();
        printTimer.scheduleAtFixedRate(printTask, 0, 100);

        stage.setOnCloseRequest(e ->
        {
            Platform.exit();
            System.exit(0);
        });

        root.getChildren().addAll(menuBar, playerBar, initializeEmptyField());
        Scene scene = new Scene(root);
        scene.getStylesheets().add("MinesweeperApp.css");
        stage.getIcons().add(bomb);
        stage.setTitle("Minesweeper Application");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.show();
    }

    public static void updateBombLabel()
    {
        ((Label) ((BorderPane) root.getChildren().get(1)).getChildren().get(2)).setText(Integer.toString(bombAmount - flagCount));
    }

    private static GridPane initializeEmptyField()
    {
        isFirstClick = true;
        GridPane gridPane = new GridPane();
        gridPane.setPrefSize(dimensions * 45, dimensions * 45);

        for (int x = 0; x < dimensions; x++)
        {
            for (int y = 0; y < dimensions; y++)
            {
                field[x][y] = new Square(x, y);
                gridPane.add(field[x][y], y, x);
            }
        }

        return gridPane;
    }

    private static void insertEmptySquares(Square square)//makes sure the player's first click isn't on a bomb or a lone number
    {
        int emptyPlaced = 0;
        Random random = new Random();

        if ((square.x == 0 && square.y == 0) || (square.x == 0 && square.y == dimensions - 1) || (square.x == dimensions - 1 && square.y == 0) || (square.x == dimensions - 1 && square.y == dimensions - 1))
        {//if is true if player clicks one of the corners
            int randEmpty = 2;//frees up two spots around the corner
            while (emptyPlaced < randEmpty)
            {
                int randX = (random.nextInt(3) - 1);
                int randY = (random.nextInt(3) - 1);
                if (((square.x + randX >= 0) && (square.x + randX < dimensions) && (square.y + randY >= 0) && (square.y + randY < dimensions) && (field[randX + square.x][randY + square.y].isEmpty == false)))
                {
                    field[randX + square.x][randY + square.y].isEmpty = true;
                    emptyPlaced++;
                }
            }
        } else if ((square.x == 0) || (square.y == 0) || (square.x == dimensions - 1) || (square.y == dimensions - 1))
        {//this if is true if player clicks the outside layer of buttons
            int randEmpty = (random.nextInt(2) + 2);//frees up 3 or 4 spots around the clicked tile
            while (emptyPlaced < randEmpty)
            {
                int randX = (random.nextInt(3) - 1);
                int randY = (random.nextInt(3) - 1);
                if (((square.x + randX >= 0) && (square.x + randX < dimensions) && (square.y + randY >= 0) && (square.y + randY < dimensions) && (field[randX + square.x][randY + square.y].isEmpty == false)))
                {
                    field[randX + square.x][randY + square.y].isEmpty = true;
                    emptyPlaced++;
                }
            }
        } else
        {//this is true if the player clicks anywhere inside
            int randEmpty = (random.nextInt(5) + 2);//frees up from 2 to 6 tiles
            while (emptyPlaced < randEmpty)
            {
                int randX = (random.nextInt(3) - 1);
                int randY = (random.nextInt(3) - 1);
                if (((square.x + randX >= 0) && (square.x + randX < dimensions) && (square.y + randY >= 0) && (square.y + randY < dimensions) && (field[randX + square.x][randY + square.y].isEmpty == false)))
                {
                    field[randX + square.x][randY + square.y].isEmpty = true;
                    emptyPlaced++;
                }
            }
        }
    }

    private static void insertNumberedSquares()//for the "empty" squares to be empty  the squares around those ones must not have bombs
    {
        for (int x = 0; x < dimensions; x++)
        {
            for (int y = 0; y < dimensions; y++)
            {
                if (field[x][y].isEmpty == true)
                {
                    for (int i = -1; i < 2; i++)
                    {
                        for (int j = -1; j < 2; j++)
                        {
                            if (!(i == 0 && j == 0))
                            {
                                if (((x + i >= 0) && (x + i < dimensions) && (y + j >= 0) && (y + j < dimensions) && (field[x + i][y + j].isEmpty == false) && (field[x + i][y + j].isNumber == false)))
                                {
                                    field[x + i][y + j].isNumber = true;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void insertBombs()//inserts the bombs into the field
    {
        int placed = 0;
        Random random = new Random();

        bombAmount = (int) (round((dimensions * dimensions) * (bombPercent / 100.0)));//amount of bombs to be placed

        while (placed < bombAmount)
        {
            int randX = random.nextInt(dimensions);
            int randY = random.nextInt(dimensions);
            if ((field[randX][randY].bombSquare == false) && (field[randX][randY].isEmpty == false) && (field[randX][randY].isNumber == false))
            {
                field[randX][randY].bombSquare = true;
                placed++;
            }
        }
    }

    private static void insertBombNums()//assigns a value to the "bombNumber" variable of each square, that value is the amount of bombs around the square
    {
        for (int x = 0; x < dimensions; x++)
        {
            for (int y = 0; y < dimensions; y++)
            {
                if (field[x][y].bombSquare == false)
                {
                    field[x][y].bombNumber = countBombNum(x, y);
                }
            }
        }
    }

    private static int countBombNum(int x, int y)//this counts the bombs around each square (is called by insertBombNums)
    {
        int count = 0;//bomb counter

        for (int i = -1; i < 2; i++)
        {
            for (int j = -1; j < 2; j++)
            {
                if ((i == 0 && j == 0) == false)//don't check the square itself
                {
                    if (((x + i >= 0) && (x + i < dimensions) && (y + j >= 0) && (y + j < dimensions) && (field[x + i][y + j].bombSquare == true)))//if not out of bounds and a bomb
                    {
                        count++;//increase the bomb counter
                    }
                }
            }
        }

        return count;
    }

    private static void colourBombNums()//assigns a colour to the Color variable of each square, according to the number of bombs around it
    {
        Color[] colours =
        {
            Color.TRANSPARENT, Color.BLUE, Color.GREEN, Color.RED, Color.DARKBLUE, Color.DARKRED, Color.CYAN, Color.BLACK, Color.DARKGRAY
        };

        for (int x = 0; x < dimensions; x++)
        {
            for (int y = 0; y < dimensions; y++)
            {
                field[x][y].colour = colours[field[x][y].bombNumber];
            }
        }
    }

    protected static void importPlayArea(Square square)//gets called when the user makes the first click on the board
    {
        insertEmptySquares(square);
        insertNumberedSquares();
        insertBombs();
        insertBombNums();
        colourBombNums();
        updateBombLabel();
        GridPane gridPane = new GridPane();
        gridPane.setPrefSize(dimensions * 45, dimensions * 45);

        root.getChildren().remove(2);

        for (int x = 0; x < dimensions; x++)
        {
            for (int y = 0; y < dimensions; y++)
            {
                gridPane.add(field[x][y], y, x);
            }
        }

        root.getChildren().add(gridPane);
    }

    private static void restart()//resets everything gets called on new game and on changing modes
    {

        field = new Square[dimensions][dimensions];
        isPaused = true;
        isOver = false;
        clickedSquares = 0;
        bombsFound = 0;
        flagCount = 0;
        secondsPassed = 0;
        flagCount = 0;
        bombAmount = (int) (round((dimensions * dimensions) * (bombPercent / 100.0)));
        updateBombLabel();

        timer.cancel();
        TimerTask task = new TimerTask()//counts how many seconds the player is playing for
        {
            @Override
            public void run()
            {
                if (!(isFirstClick))
                {
                    secondsPassed++;
                }
            }
        ;
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 1000);

        root.getChildren().remove(2);
        root.getChildren().add(initializeEmptyField());//makes new (empty) field
        primaryStage.sizeToScene();
    }

    protected static void victory()
    {
        isOver = true;
        Alert won = new Alert(AlertType.CONFIRMATION);
        won.setGraphic(new ImageView(victory));
        won.setTitle("You Won!");
        won.setHeaderText("Congratulations!");
        won.setContentText("You found all the bombs in " + secondsPassed + " seconds.");
        won.showAndWait();

        restart();
    }

    protected static void defeat(Square square)
    {
        isOver = true;//for the timer to pause
        for (int x = 0; x < dimensions; x++)
        {
            for (int y = 0; y < dimensions; y++)//goes through the entire field
            {
                if (field[x][y].isFlagged)//if it's flagged
                {
                    if (field[x][y].bombSquare)//show succesful finds
                    {
                        field[x][y].tile.setGraphic(new ImageView(flaggedBomb));
                        field[x][y].tile.setDisable(true);
                    } else//show unsuccessful finds
                    {
                        field[x][y].tile.setGraphic(new ImageView(notBomb));
                        field[x][y].tile.setDisable(true);
                    }
                } else//if not flagged
                {
                    if (field[x][y].bombSquare)//shows remaining bombs
                    {
                        field[x][y].tile.setGraphic(new ImageView(bomb));
                        field[x][y].tile.setDisable(true);
                    }
                }
            }
        }
        square.tile.setGraphic(new ImageView(fatalBomb));//shows the click that caused the loss
        Alert gameLost = new Alert(AlertType.INFORMATION);
        gameLost.setGraphic(new ImageView(defeat));
        gameLost.setTitle("Game Over!");
        gameLost.setHeaderText("Bombs Exploded!");
        gameLost.setContentText("Oh no! You Clicked on a bomb causing you to lose! Better luck next time.");
        gameLost.showAndWait();

        restart();

    }

    public static void main(String[] args)//MAIN
    {
        launch(args);
    }

}
