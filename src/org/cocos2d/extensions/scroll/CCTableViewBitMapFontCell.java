/*
 *	Port from SWScrollView and SWTableView for iphone 
 *	by Rodrigo Collavo on 02/03/2012
 */

package org.cocos2d.extensions.scroll;
 
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.types.CGPoint;

public class CCTableViewBitMapFontCell extends CCTableViewCell {

private CCBitmapFontAtlas m_sprite ;

public void setSprite(CCBitmapFontAtlas s, CCSprite image) {
    if (m_sprite != null) {
        removeChild(m_sprite, false);
    }
    
     
    image.setAnchorPoint(CGPoint.ccp(0f,1f));
    image.setPosition(CGPoint.ccp(0, image.getContentSize().height));
    s.setAnchorPoint(CGPoint.ccp(0.5f,0f));
    s.setPosition(CGPoint.ccp(image.getContentSize().width/2, 0));
    m_sprite = s;
    image.addChild(m_sprite);
    addChild(image);
}

public CCBitmapFontAtlas getSprite() {
    return m_sprite;
}

@Override
public void reset() {
	super.reset();

    if (m_sprite != null) {
        removeChild(m_sprite, false);
    }
    m_sprite = null;
}
}
