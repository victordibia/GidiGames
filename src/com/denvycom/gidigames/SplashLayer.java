package com.denvycom.gidigames;


import org.cocos2d.actions.base.CCRepeatForever;
import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCDelayTime;
import org.cocos2d.actions.interval.CCScaleBy;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCTextureCache;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.particlesystem.CCParticleExplosion;
import org.cocos2d.particlesystem.CCParticleFire;
import org.cocos2d.particlesystem.CCParticleFireworks;
import org.cocos2d.particlesystem.CCParticleSystem;
import org.cocos2d.transitions.CCSlideInLTransition;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor4B;

import android.database.Cursor;





public class SplashLayer extends CCColorLayer {
	DbAdapter mDbHelper ;
	private CCParticleSystem emitter;

	public static CCScene scene()
	{
		CCScene scene = CCScene.node();
		CCLayer layer = new SplashLayer();

		scene.addChild(layer);

		return scene;
	}

	protected SplashLayer()
	{
		super(ccColor4B.ccc4(0, 0, 0, 0)); 
		CGSize screenSize = CCDirector.sharedDirector().winSize();


		float tilescalefactor = screenSize.height / 480 ;
		//Add Lexis Logo
		CCSprite gidilogo = CCSprite.sprite("gidilogo.png"); 
		gidilogo.setScale(0.8f *tilescalefactor);
		gidilogo.setPosition(CGPoint.ccp(screenSize.width / 2.0f    , screenSize.height / 2.0f));
		CCScaleBy scalelexis =  CCScaleBy.action(1.2f, 1.1f);
		CCSequence lexisseq = CCSequence.actions(scalelexis, scalelexis.reverse());
		gidilogo.runAction(CCRepeatForever.action(lexisseq));
		//background.addChild(lexis,2) ;
		addChild(gidilogo,2) ;


		//emitter.runAction(CCMoveTo.action(3f,CGPoint.ccp(screenSize.width, 100)));

		//Add Denvycom Logo
		CCSprite denvycomlogo = CCSprite.sprite("denvycom.png"); 
		denvycomlogo.setScale(GidiGamesActivity.scalefactor);
		denvycomlogo.setPosition(CGPoint.make(screenSize.width - denvycomlogo.getContentSize().width/2.0f * GidiGamesActivity.scalefactor , denvycomlogo.getContentSize().height *tilescalefactor -  ((denvycomlogo.getContentSize().height * GidiGamesActivity.scalefactor)/2)   ));
		addChild(denvycomlogo,2) ;

		//Add loading text

		CCBitmapFontAtlas loadingAtlas = CCBitmapFontAtlas.bitmapFontAtlas( "Loading ...", "dalek.fnt");
		loadingAtlas.setScale(0.9f*tilescalefactor);
		loadingAtlas.setPosition(CGPoint.make( loadingAtlas.getContentSize().width * GidiGamesActivity.scalefactor , loadingAtlas.getContentSize().height *tilescalefactor -  ((loadingAtlas.getContentSize().height * GidiGamesActivity.scalefactor)/2)   ));

       addChild(loadingAtlas,5);
		//ccMacros.CCLOG("Sizing after",  denvycomlogo.getContentSize().width + " ---------------------");

		this.runAction(CCSequence.actions(CCDelayTime.action(1.0f), CCCallFuncN.action(this, "loadMenu")));

	}


	public void loadMenu(Object theSprite) {

		GidiGamesActivity.checkWordBank();
		CCScene next = CCSlideInLTransition.transition(0.2f, MenuLayer.scene());
		CCDirector.sharedDirector().replaceScene( next );


		/* emitter = CCParticleFireworks.node();
          addChild(emitter, 10);

         emitter.setTexture(CCTextureCache.sharedTextureCache().addImage("stars.png"));
         emitter.setScale( CCDirector.sharedDirector().winSize().width/480) ;

         emitter.setAngle(180);
         emitter.setBlendAdditive(true);*/
		//setEmitterPosition();


	}



}



