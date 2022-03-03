package minesweeperapp;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

class Square extends GridPane
{

    static Image flag = new Image("flag.png");
    Button tile = new Button();
    Color colour;
    int x, y, bombNumber;
    boolean bombSquare, isClicked, isFlagged, isEmpty, isNumber;

    public Square(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.bombNumber = 0;
        this.bombSquare = false;
        this.isClicked = false;
        this.isFlagged = false;
        this.colour = null;
        this.isEmpty = false;
        this.isNumber = false;

        tile.setMinSize(45, 45);
        tile.setMaxSize(45, 45);
        tile.setPrefSize(45, 45);

        tile.setOnMouseClicked(e -> clicked(e));//on player click

        getChildren().addAll(tile);
    }

    private void clicked(MouseEvent e)
    {
        if (e.getButton() == MouseButton.PRIMARY)//LEFT click
        {
            if (MinesweeperApp.isFirstClick)//if its the first click
            {
                isEmpty = true;//set the clicked button to a guaranteed "empty"
                MinesweeperApp.isPaused = false;
                MinesweeperApp.isFirstClick = false;
                MinesweeperApp.importPlayArea(this);//makes the field
            }
            if (!isFlagged)//if the square does not have a flag
            {
                tile.setBackground(null);
                tile.setDisable(true);
                isClicked = true;
                MinesweeperApp.clickedSquares++;//for win condition
                if (bombSquare)//if the square has a bomb
                {
                    MinesweeperApp.defeat(this);//the player loses
                } else
                {
                    if (bombNumber == 0)//if the square is a "blank"
                    {
                        blankClicked(this);
                    } else//if the square has a number
                    {
                        tile.setText(Integer.toString(bombNumber));
                        tile.setTextFill(colour);
                    }
                }
            }
        } else//RIGHT click
        {
            if (!(MinesweeperApp.isFirstClick))//don't let player set flags if he hasn't started the game
            {
                if (!isFlagged)//if the square does not have a flag
                {
                    isFlagged = true;
                    MinesweeperApp.flagCount++;
                    MinesweeperApp.updateBombLabel();
                    tile.setGraphic(new ImageView(flag));
                    if (bombSquare)
                    {
                        MinesweeperApp.bombsFound++;
                    }
                } else//if the square does have a flag
                {
                    if (bombSquare)//if the square has a bomb
                    {
                        MinesweeperApp.bombsFound--;
                    }
                    MinesweeperApp.flagCount--;
                    MinesweeperApp.updateBombLabel();
                    tile.setGraphic(null);
                    isFlagged = false;
                }
            }
        }

        //win conditions
        if (MinesweeperApp.bombsFound == MinesweeperApp.bombAmount)//if the player has flagged all the bombs
        {
            if ((MinesweeperApp.flagCount - MinesweeperApp.bombsFound) == 0)//AND he hasn't flagged anything extra
            {
                if (((MinesweeperApp.dimensions * MinesweeperApp.dimensions) - MinesweeperApp.bombAmount) == MinesweeperApp.clickedSquares)//AND all squares are clicked (in case player has a few free squares left and doesn't abuse trying flagging all possible combinations to win)
                {
                    MinesweeperApp.victory();//he wins
                }
            }
        }
    }

    private void blankClicked(Square square)
    {
        for (int i = -1; i < 2; i++)
        {
            for (int j = -1; j < 2; j++)
            {
                if ((i == 0 && j == 0) == false)//don't check the square itself
                {
                    if ((square.x + i >= 0) && (square.x + i < MinesweeperApp.dimensions) && (square.y + j >= 0) && (square.y + j < MinesweeperApp.dimensions))//if not out of bounds
                    {
                        Square neighbour = MinesweeperApp.field[square.x + i][square.y + j];
                        if (!(neighbour.isClicked) && !(neighbour.bombSquare))//if the square is not clicked and doesn't have a bomb
                        {
                            neighbour.tile.setDisable(true);
                            neighbour.tile.setGraphic(null);
                            neighbour.tile.setText(Integer.toString(neighbour.bombNumber));
                            neighbour.tile.setTextFill(neighbour.colour);
                            neighbour.isClicked = true;
                            MinesweeperApp.clickedSquares++;
                            if (neighbour.isFlagged)//if the neighbour is flagged(but doesn't have a bomb)
                            {
                                neighbour.isFlagged = false;
                                MinesweeperApp.flagCount--;
                                MinesweeperApp.updateBombLabel();
                            }
                            if ((neighbour.bombNumber == 0))//if the neighbour is also blank
                            {
                                blankClicked(neighbour);
                            }
                        }
                    }
                }
            }
        }
    }

}
