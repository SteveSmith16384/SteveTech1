uniform sampler2D m_Texture;
uniform sampler2D m_OutlineDepthTexture;
uniform sampler2D m_DepthTexture;
varying vec2 texCoord;

uniform vec2 m_Resolution;
uniform vec4 m_OutlineColor;
uniform float m_OutlineWidth;

void main() {
	vec4 depth = texture2D(m_OutlineDepthTexture, texCoord);
	vec4 depth1 = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(m_OutlineWidth,m_OutlineWidth))/m_Resolution);
	vec4 depth2 = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(m_OutlineWidth,-m_OutlineWidth))/m_Resolution);
	vec4 depth3 = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(-m_OutlineWidth,m_OutlineWidth))/m_Resolution);
	vec4 depth4 = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(-m_OutlineWidth,-m_OutlineWidth))/m_Resolution);
	vec4 depth5 = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(0.,m_OutlineWidth))/m_Resolution);
	vec4 depth6 = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(0.,-m_OutlineWidth))/m_Resolution);
	vec4 depth7 = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(m_OutlineWidth,0.))/m_Resolution);
	vec4 depth8 = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(-m_OutlineWidth,0.))/m_Resolution);
	vec4 color = texture2D(m_Texture, texCoord);
	//如果是背景
	float ratio=0.;
	if(depth==vec4(0.) && (depth1 != depth || depth2 != depth || depth3 != depth || depth4 != depth||depth5 != depth || depth6 != depth || depth7 != depth || depth8 != depth)){
		float dist=m_OutlineWidth;
		//距离边的像素
		vec4 nearDepth;
		if(depth1 != depth){
			for(float i=0.;i<m_OutlineWidth;i++){
				nearDepth = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(i,i))/m_Resolution);
				if(nearDepth != depth){
					dist = i;
					break;
				}
			}
		}else
		if(depth2 != depth){
			for(float i=0.;i<m_OutlineWidth;i++){
				nearDepth = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(i,-i))/m_Resolution);
				if(nearDepth != depth){
					dist = i;
					break;
				}
			}
		}else
		if(depth3 != depth){
			for(float i=0.;i<m_OutlineWidth;i++){
				nearDepth = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(-i,i))/m_Resolution);
				if(nearDepth != depth){
					dist = i;
					break;
				}
			}
		}else
		if(depth4 != depth){
			for(float i=0.;i<m_OutlineWidth;i++){
				nearDepth = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(-i,-i))/m_Resolution);
				if(nearDepth != depth){
					dist = i;
					break;
				}
			}
		}else
		if(depth5 != depth){
			for(float i=0.;i<m_OutlineWidth;i++){
				nearDepth = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(0.,i))/m_Resolution);
				if(nearDepth != depth){
					dist = i;
					break;
				}
			}
		}else
		if(depth6 != depth){
			for(float i=0.;i<m_OutlineWidth;i++){
				nearDepth = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(0.,-i))/m_Resolution);
				if(nearDepth != depth){
					dist = i;
					break;
				}
			}
		}else
		if(depth7 != depth){
			for(float i=0.;i<m_OutlineWidth;i++){
				nearDepth = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(i,0.))/m_Resolution);
				if(nearDepth != depth){
					dist = i;
					break;
				}
			}
		}else
		if(depth8 != depth){
			for(float i=0.;i<m_OutlineWidth;i++){
				nearDepth = texture2D(m_OutlineDepthTexture, ((texCoord*m_Resolution)+vec2(-i,0.))/m_Resolution);
				if(nearDepth != depth){
					dist = i;
					break;
				}
			}
		}
		//0:场景颜色		1:outline颜色 
		ratio = clamp(1.- dist/m_OutlineWidth,0.,1.);
		//float off = (1.-ratio*ratio)*(1.-ratio*ratio);
		gl_FragColor = color*(1.-ratio) +m_OutlineColor*ratio;
		//gl_FragColor = m_OutlineColor;
	}else{
		gl_FragColor = color;
	}
	//debug
	//gl_FragColor = vec4(0.,(1.-ratio),0.,1.);
}