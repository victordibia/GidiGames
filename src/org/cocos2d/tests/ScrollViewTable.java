package org.cocos2d.tests;

import java.util.ArrayList;
import java.util.Random;

import org.cocos2d.actions.UpdateCallback;
import org.cocos2d.actions.base.CCAction;
import org.cocos2d.actions.base.CCRepeatForever;
import org.cocos2d.actions.interval.CCIntervalAction;
import org.cocos2d.actions.interval.CCMoveBy;
import org.cocos2d.actions.interval.CCRotateBy;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.events.CCTouchDispatcher;
import org.cocos2d.extensions.scroll.CCScrollView;
import org.cocos2d.extensions.scroll.CCTableView;
import org.cocos2d.extensions.scroll.CCTableViewCell;
import org.cocos2d.extensions.scroll.CCTableViewDataSource;
import org.cocos2d.extensions.scroll.CCTableViewDelegate;
import org.cocos2d.extensions.scroll.CCTableViewSpriteCell;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCLabel;
import org.cocos2d.nodes.CCMotionStreak;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.opengl.CCGLSurfaceView;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;
import org.cocos2d.types.ccColor4B;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class ScrollViewTable extends Activity {
    // private static final String LOG_TAG = CocosNodeTest.class.getSimpleName();
    private CCGLSurfaceView mGLSurfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mGLSurfaceView = new CCGLSurfaceView(this);
        setContentView(mGLSurfaceView);
    }

    @Override
    public void onStart() {
        super.onStart();

        // attach the OpenGL view to a window
        CCDirector.sharedDirector().attachInView(mGLSurfaceView);

        // set landscape mode
        CCDirector.sharedDirector().setLandscape(false);

        // show FPS
        CCDirector.sharedDirector().setDisplayFPS(true);

        // frames per second
        CCDirector.sharedDirector().setAnimationInterval(1.0f / 60);

        CCScene scene = CCScene.node();
        ArrayList<String> array = new ArrayList<String>();
	    String iconName;
	    for (int i = 0; i < 30; i++) {
	        iconName = new String("grossini.png");
	        array.add(iconName);
	    }
        scene.addChild(new ScrollTableViewMenu(array));

        // Make the Scene active
        CCDirector.sharedDirector().runWithScene(scene);

    }

    @Override
    public void onPause() {
        super.onPause();

        CCDirector.sharedDirector().onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        CCDirector.sharedDirector().onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        CCDirector.sharedDirector().end();
        // CCTextureCache.sharedTextureCache().removeAllTextures();
    }

 

    static class ScrollTableViewMenu extends CCLayer implements CCTableViewDelegate, CCTableViewDataSource{

        private CGSize cellSize_;

    	private CCTableView tableView_;
    	private ArrayList<String> elements_;
    	

    	// You need to send an array of sprite names, check on commented code in ScrollView example
    	public ScrollTableViewMenu(ArrayList<String> array) {
    	    cellSize_ = CGSize.make(77, 78);
    	    setIsTouchEnabled(true);
    	    elements_ = array;
    	    
//    		CCLayerColor *clipping = CCLayerColor::layerWithColor(ccc4(255, 255, 255, 255));
//    	    clipping.setPosition(ccp(50, 100));
//    	    clipping.setContentSize(CGSizeMake(100, 300));
    	//    
//    	    addChild(clipping);
    	    
    	    CGSize winSize = CCDirector.sharedDirector().winSize();
    	    tableView_ = CCTableView.view(this, CGSize.make(77, 300));//winSize.width, 57));
    	    tableView_.tDelegate = this;
    	    tableView_.dataSource = this;
//    	    tableView_.setClipsToBounds(true);
//    	    tableView_.setViewSize(CGSizeMake(100, 300));
    	    tableView_.setPosition(CGPoint.ccp(50, 100));

    	    //tableView_.setDirection(SWScrollViewDirectionHorizontal);
    	    tableView_.setVerticalFillOrder(CCTableView.CCTableViewFillTopDown);

    	    addChild(tableView_);
    	    
    	    //CCSprite *image = CCSprite::spriteWithFile("Icon.png");
    	    //tableView_.addChild(image);
    	    tableView_.reloadData();
    	}

    	public void setPosition(CGPoint position)
    	{
    	    tableView_.setPosition(position);
    	}

    	public CGPoint getPosition()
    	{
    	    return tableView_.getPosition();
    	}

    	public static ScrollTableViewMenu menu(ArrayList<String> array)
    	{
    		return new ScrollTableViewMenu(array);
    	}

    	public void registerWithTouchDispatcher()
    	{
    	    CCTouchDispatcher.sharedDispatcher().addTargetedDelegate(this, 0, true);
    	}

    	public boolean containsTouchLocation(MotionEvent event)
    	{
    	    CGSize s = tableView_.viewSize;
    	    CGRect r = CGRect.make(getPosition().x, getPosition().y, s.width, s.height);
    	    return CGRect.containsPoint(r, convertTouchToNodeSpace(event));
    	}

    	@Override
    	public boolean ccTouchesBegan(MotionEvent event) {
    	    if (!containsTouchLocation(event)) return false;
    	    return true;
    	}

    	//SWTableViewDelegate Protocol
    	public void tableCellTouched(CCTableView table, CCTableViewCell cell)
    	{
    	    //setPosition(ccp(getPosition().x, getPosition().y + cellSize_.height));
    	}

    	//SWTableViewDataSource Protocol
    	public CGSize cellSizeForTable(CCTableView table)
    	{
    	    return cellSize_;
    	}

    	public CCTableViewCell tableCellAtIndex(CCTableView table, int idx)
    	{
    	    CCTableViewSpriteCell cell = (CCTableViewSpriteCell) table.dequeueCell();
    	    if (cell == null) {
    	        cell = new CCTableViewSpriteCell();
    	    }

    	    String name = (String) elements_.get(idx);
    	    CCSprite image = CCSprite.sprite(name);
    	    image.setColor(ccColor3B.ccc3(255/(idx + 1), 255/(idx + 1), 255));
    	    cell.setSprite(image);
    	    cell.getSprite().setPosition(CGPoint.ccp((cellSize_.width - image.getContentSize().width) /2, (cellSize_.height - image.getContentSize().height) / 2));
    	    return cell;
    	}

    	public int numberOfCellsInTableView(CCTableView table)
    	{
    	    return elements_.size();
    	}
    }

}

