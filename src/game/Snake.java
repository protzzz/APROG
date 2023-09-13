package game;

import java.awt.*;
import java.util.ArrayList;

public class Snake {
    public static final Color SNAKE_COLOUR = new Color(50, 205, 50);
    public final boolean WALL_COLLISION;
    private final int LEFT_BOUND, TOP_BOUND, BOTTOM_BOUND, RIGHT_BOUND;

    private ArrayList<Point> bodyPartsList;
    private int snakeBodySize, mapHeight, mapWidth;
    private Point tailLastLocation;

    public Snake(Point startLoc, boolean wallCollision, int snakeBodySize, int mapHeight, int mapWidth)
    {
        bodyPartsList = new ArrayList<Point>();
        bodyPartsList.add(startLoc);

        this.WALL_COLLISION = wallCollision;
        this.snakeBodySize = snakeBodySize;
        this.mapHeight = mapHeight;
        this.mapWidth = mapWidth;

        this.LEFT_BOUND = 0;
        this.TOP_BOUND = 0;
        this.BOTTOM_BOUND = mapHeight / snakeBodySize;
        this.RIGHT_BOUND = mapWidth / snakeBodySize;
    }

    public final ArrayList<Point> getBodyPartsList()
    {
        return bodyPartsList;
    }

    public Point getTailLastLocation()
    {
        return tailLastLocation;
    }

    public void addBodyPart(Point loc)
    {
        bodyPartsList.add(loc);
    }

    public void addBodyPart(Direction dir)
    {
        Point tailLocation = getDirectionOffset(bodyPartsList.get(bodyPartsList.size() - 1), dir);
        bodyPartsList.add(tailLocation);

        tailLastLocation = tailLocation;
    }

    public CollisionType move(Direction dir)
    {
        tailLastLocation = bodyPartsList.get(bodyPartsList.size() - 1);

        Point previousLocation = bodyPartsList.get(0);
        Point newHeadLoc = getDirectionOffset(previousLocation, dir);

        if (willCollideWithBody(newHeadLoc))
        {
            return CollisionType.Body;
        }

        if (WALL_COLLISION && willGoOutOfBounds(newHeadLoc))
        {
            return CollisionType.Wall;
        }

        bodyPartsList.set(0, newHeadLoc);

        for (int i = 1; i < bodyPartsList.size(); i++)
        {
            Point tempPreviousLocation = bodyPartsList.get(i);

            bodyPartsList.set(i, previousLocation);

            previousLocation = tempPreviousLocation;
        }

        return CollisionType.None;
    }

    public boolean willCollideWithBody(Point headLoc)
    {
        for (int i = 1; i < bodyPartsList.size() - 1; i++)
        {
            if (headLoc.equals(bodyPartsList.get(i)))
            {
                return true;
            }
        }

        return false;
    }

    public boolean willGoOutOfBounds(Point headLoc)
    {
        return headLoc.y < 0 || headLoc.x < 0  || headLoc.y >= mapHeight / snakeBodySize || headLoc.x >= mapWidth / snakeBodySize;
    }

    private Point getDirectionOffset(Point initialPoint, Direction dir)
    {
        Point offsetPoint = (Point)initialPoint.clone();

        if (dir == Direction.Down)
        {
            offsetPoint.y++;
        }

        if (dir == Direction.Left)
        {
            offsetPoint.x--;
        }

        if (dir == Direction.Right)
        {
            offsetPoint.x++;
        }

        if (dir == Direction.Up)
        {
            offsetPoint.y--;
        }

        handleWallTeleportation(offsetPoint);

        return offsetPoint;
    }

    private void handleWallTeleportation(Point headLoc)
    {
        if (!WALL_COLLISION)
        {
            if (headLoc.y < TOP_BOUND)
            {
                headLoc.y = BOTTOM_BOUND - 1;
            }

            if (headLoc.x < LEFT_BOUND)
            {
                headLoc.x = RIGHT_BOUND - 1;
            }

            if (headLoc.y >= BOTTOM_BOUND)
            {
                headLoc.y = TOP_BOUND;
            }

            if (headLoc.x >= RIGHT_BOUND)
            {
                headLoc.x = LEFT_BOUND;
            }
        }
    }
}