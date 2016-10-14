# SwipeBackLayout
##HOW TO USE
copy the source code and attach android activity
```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        SwipeBack swipeBack = SwipeBack.attachActivity(this);
    }
```
##CONFIG
* set support drag region 
```java
@IntDef(value = {DRAG_NONE, DRAG_FULL, EDGE_LEFT, EDGE_RIGHT, EDGE_TOP, EDGE_BOTTOM, EDGE_ALL}, flag = true)
@Retention(RetentionPolicy.SOURCE)
@interface DragMode {

    }
swipeBack.setDragMode(SwipeBack.EDGE_LEFT);
```
* set support drag direction
```java
@IntDef(value = {DIRECTION_LEFT, DIRECTION_RIGHT, DIRECTION_TOP, DIRECTION_BOTTOM}, flag = true)
@Retention(RetentionPolicy.SOURCE)
@interface Direction {

    }
swipeBack.setDirection(SwipeBack.DIRECTION_LEFT);
```

