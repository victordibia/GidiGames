package com.denvycom.gidigames;


import java.util.ArrayList;

import org.cocos2d.actions.interval.CCDelayTime;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.extensions.scroll.CCScrollView;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.opengl.CCTextureAtlas;
import org.cocos2d.transitions.CCSlideInLTransition;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;
import org.cocos2d.types.ccColor4B;
 





public class LexisHelpLayer extends CCColorLayer {

	private static final int MAIN_WORD_NODE_LABEL_TAG = 1;
	private static final int HELP_MENU_TAG = 280; 
	CCTextureAtlas textureAtlas;
	private float tilescalefactor = 0.0f;
	CCScrollView scrollView;

	public static CCScene scene()
	{ 
		GidiGamesActivity.nextscene= "LexisMenuLayer" ;
		 GidiGamesActivity.currentscene = "LexisHelpLayer" ;
		CCScene scene = CCScene.node();
		CCLayer layer = new LexisHelpLayer();
		scene.addChild(layer);

		return scene;
	}


	protected LexisHelpLayer()
	{
		super(ccColor4B.ccc4(0, 0, 0, 0));

		this.setIsKeyEnabled(true) ; 
		CGSize screenSize = CCDirector.sharedDirector().winSize();


		tilescalefactor   = (screenSize.height/400) ;
		//SoundEngine.sharedEngine().playSound(context, R.raw.jungle, true);

		//Add Background Sprite Image 
		CCSprite background = CCSprite.sprite("background.jpg");
		background.setScale(screenSize.width / background.getContentSize().width);
		background.setAnchorPoint(CGPoint.ccp(0f,1f)) ;
		background.setPosition(CGPoint.ccp(0, screenSize.height));
		background.setColor(ccColor3B.ccc3(230, 230, 230));
		addChild(background,1); 



		//Add Top Scroll
		CCSprite topscroll = CCSprite.sprite("darktranstop.png");
		float picscale = screenSize.width/topscroll.getContentSize().width ;
		topscroll.setScale(picscale);
		topscroll.setPosition(CGPoint.ccp(screenSize.width / 2.0f, screenSize.height + topscroll.getContentSize().height));
		addChild(topscroll,15);
		topscroll.runAction(CCSequence.actions(
				CCDelayTime.action(0.4f),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - (topscroll.getContentSize().height * picscale)/2)), 
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - (topscroll.getContentSize().height * picscale)/2 + 20 * picscale )) 
				));





		CCMenuItemImage backbtn = CCMenuItemImage.item("backbutton.png", "backbutton.png",this, "backCallback");
		backbtn.setScale(0.6f * tilescalefactor);


		CCMenu backmenu = CCMenu.menu(backbtn); 
		backmenu.setContentSize(backbtn.getContentSize());
		backmenu.setPosition(CGPoint.make(0, 0));
		backbtn.setPosition(CGPoint.make(backbtn.getContentSize().width * tilescalefactor/2, backbtn.getContentSize().height*tilescalefactor/2));

		addChild(backmenu, 100,HELP_MENU_TAG);


		// Add Title
		CCBitmapFontAtlas selectgame = CCBitmapFontAtlas.bitmapFontAtlas ("HOW TO PLAY LEXIS!", "dalek.fnt");
		//selectgame.setAnchorPoint(CGPoint.ccp(0,1));
		selectgame.setColor(ccColor3B.ccc3(105, 75, 41));
		selectgame.setScale( GidiGamesActivity.scalefactor + 0.4f * GidiGamesActivity.scalefactor);
		selectgame.setAnchorPoint(1f,1f);
		selectgame.setPosition(CGPoint.ccp(screenSize.width + selectgame.getContentSize().width + 40 , screenSize.height -20 ));
		addChild(selectgame,16,MAIN_WORD_NODE_LABEL_TAG);
		selectgame.runAction(CCSequence.actions(
				CCDelayTime.action(0.5f),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width - 20.0f * picscale, screenSize.height - 25 * picscale )) 
				));


		float scrollboxheight = (topscroll.getContentSize().height * picscale) - (30 * picscale) ;


		String helptext = "1.) Lexis is a language learning game \n" +
				"2.) Select a Language and click start \n"	 +
				"3.) Slash at the word that correctly translates  \n   the green word at top right \n"	+
				"4.) Get points by correct slashes \n" +
				"5.) FINISH in time too!"	;
		CCBitmapFontAtlas helpFontAtlas = CCBitmapFontAtlas.bitmapFontAtlas( helptext, "dalek.fnt");
		helpFontAtlas.setScale(0.9f*tilescalefactor);
		helpFontAtlas.setPosition(screenSize.width/2.0f - helpFontAtlas.getContentSize().width/2 * tilescalefactor, scrollboxheight + (50 * picscale) -  helpFontAtlas.getContentSize().height/2 *tilescalefactor  ) ;
		 

		scrollView = CCScrollView.view(CGSize.zero()); 
 

 
		scrollView.setContentSize(CGSize.make(screenSize.width,  screenSize.height - scrollboxheight - 30*tilescalefactor)); // You need to set contentSize to enable scrolling.
		scrollView.bounces = true ;
		scrollView.setClipToBounds( true) ;
		scrollView.direction =2 ; 
 
 
		scrollView.addChild(helpFontAtlas, 1);
 
		addChild(scrollView,10);


		scrollView.setViewSize(CGSize.make(screenSize.width - (50*tilescalefactor) , screenSize.height   - scrollboxheight));
		scrollView.setPosition(CGPoint.ccp((25 * tilescalefactor), (backbtn.getContentSize().height + 30) * 0.6f* tilescalefactor));






	}


	public void backCallback(Object sender) {
		 
		CCScene next = CCSlideInLTransition.transition(0.2f, LexisMenuLayer.scene());
		CCDirector.sharedDirector().replaceScene( next );
	}
}



