package org.example;

import java.util.*;

import static org.example.Picture.*;

/**
 * Hello world!
 *
 */
public class App 
{
    static Picture[][] maze = new Picture[][]{
            new Picture[] {RACKET,    WINE,      RACKET,    PUMPKIN,   RACKET,    PUMPKIN,   GINKGO,  PERSIMMON},
            new Picture[] {PUMPKIN,   GINKGO,    PALETTE,   WINE,      PERSIMMON, PALETTE,   PUMPKIN, RACKET},
            new Picture[] {GINKGO,    PERSIMMON, WINE,      GINKGO,    RACKET,    WINE,      GINKGO,  PALETTE},
            new Picture[] {PALETTE,   PUMPKIN,   PALETTE,   PERSIMMON, PUMPKIN,   WINE,      RACKET,  WINE},
            new Picture[] {WINE,      GINKGO,    PUMPKIN,   WINE,      GINKGO,    PERSIMMON, PALETTE, PUMPKIN},
            new Picture[] {PERSIMMON, PALETTE,   PERSIMMON, PERSIMMON, RACKET,    RACKET,    WINE,    GINKGO},
            new Picture[] {RACKET,    RACKET,    PALETTE,   GINKGO,    PALETTE,   PUMPKIN,   PALETTE, PERSIMMON}
    };


    public static void main( String[] args )
    {
        System.out.println( "Hello Maze!" );

        List<Cell> nexts = Arrays.asList(new Cell(0, 0), new Cell(0, 1), new Cell(0, 2), new Cell(0, 3),
                new Cell(0, 4), new Cell(0, 5), new Cell(0, 6));

        for (Cell next: nexts) {
            List<Cell> path = new ArrayList<>();
            Set<Picture> pictures = new HashSet<>();
            boolean find = search(next, path, pictures);
            if (find) {
                System.out.println(path);
                System.out.println(pictures);
                break;
            }
        }

    }

    private static boolean search(Cell current, List<Cell> path, Set<Picture> pictures) {
        if (!isSafe(current, path, pictures)) {
            return false;
        }

        Picture currentPicture = maze[current.y()][current.x()];
        boolean newPicture = !pictures.contains(currentPicture);
        pictures.add(currentPicture);
        path.add(current);

        if (isGoal(current)) {
            System.out.println("Goal!");
            return true;
        }

        List<Cell> nexts = calcNexts(current);
        for (Cell next: nexts) {
            boolean find = search(next, path, pictures);
            if (find) {
                return true;
            }
        }

        // 経路が見つからなかったので、経路データから現在位置を取り除いて戻る。
        if (newPicture) {
            pictures.remove(currentPicture);
        }
        path.remove(current);
        return false;
    }

    private static List<Cell> calcNexts(Cell pos) {
        if (pos.x() == 0) {
            return Arrays.asList(new Cell(pos.x() + 1, pos.y()));
        }

        if (pos.y() == 0) {
            return Arrays.asList(new Cell(pos.x() - 1, pos.y()), new Cell(pos.x() + 1, pos.y()), new Cell(pos.x(), pos.y() + 1));
        } else if (pos.y() == 6) {
            return Arrays.asList(new Cell(pos.x() - 1, pos.y()), new Cell(pos.x() + 1, pos.y()), new Cell(pos.x(), pos.y() - 1));
        } else {
            return Arrays.asList(new Cell(pos.x() - 1, pos.y()), new Cell(pos.x() + 1, pos.y()), new Cell(pos.x(), pos.y() - 1), new Cell(pos.x(), pos.y() + 1));
        }
    }

    private static boolean isGoal(Cell pos) {
        return pos.x() == 7;
    }

    private static boolean isSafe(Cell pos, List<Cell> path, Set<Picture> pictures) {
        // 絵の種類のチェック
        Picture currentPicture = maze[pos.y()][pos.x()];
        if (pictures.size() == 3 && !pictures.contains(currentPicture)) {
            // 3種類を超えたので探索失敗。
            return false;
        } else if (path.contains(pos)) {
            // 同じ経路を辿っていないかをチェック
            return false;
        }

        return true;
    }
}
