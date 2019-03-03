/*
 * Copyright 2018 JetBrains s.r.o.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#ifndef HEADLESS

#include "sun_java2d_metal_MTLMaskFill.h"

#include "MTLMaskFill.h"
#include "MTLRenderQueue.h"
#include "MTLVertexCache.h"

/**
 * This implementation first copies the alpha tile into a texture and then
 * maps that texture to the destination surface.  This approach appears to
 * offer the best performance despite being a two-step process.
 *
 * When the source paint is a Color, we can simply use the GL_MODULATE
 * function to multiply the current color (already premultiplied with the
 * extra alpha value from the AlphaComposite) with the alpha value from
 * the mask texture tile.  In picture form, this process looks like:
 *
 *                        A     R    G     B
 *     primary color      Pa    Pr   Pg    Pb    (modulated with...)
 *     texture unit 0     Ca    Ca   Ca    Ca
 *     ---------------------------------------
 *     resulting color    Ra    Rr   Rg    Rb
 *
 * where:
 *     Px = current color (already premultiplied by extra alpha)
 *     Cx = coverage value from mask tile
 *     Rx = resulting color/alpha component
 *
 * When the source paint is not a Color, it means that we are rendering with
 * a complex paint (e.g. GradientPaint, TexturePaint).  In this case, we
 * rely on the GL_ARB_multitexture extension to effectively multiply the
 * paint fragments (autogenerated on texture unit 1, see the
 * MTLPaints_Set{Gradient,Texture,etc}Paint() methods for more details)
 * with the coverage values from the mask texture tile (provided on texture
 * unit 0), all of which is multiplied with the current color value (which
 * contains the extra alpha value).  In picture form:
 *
 *                        A     R    G     B
 *     primary color      Ea    Ea   Ea    Ea    (modulated with...)
 *     texture unit 0     Ca    Ca   Ca    Ca    (modulated with...)
 *     texture unit 1     Pa    Pr   Pg    Pb
 *     ---------------------------------------
 *     resulting color    Ra    Rr   Rg    Rb
 *
 * where:
 *     Ea = extra alpha
 *     Cx = coverage value from mask tile
 *     Px = gradient/texture paint color (generated for each fragment)
 *     Rx = resulting color/alpha component
 *
 * Here are some descriptions of the many variables used in this method:
 *   x,y     - upper left corner of the tile destination
 *   w,h     - width/height of the mask tile
 *   x0      - placekeeper for the original destination x location
 *   tw,th   - width/height of the actual texture tile in pixels
 *   sx1,sy1 - upper left corner of the mask tile source region
 *   sx2,sy2 - lower left corner of the mask tile source region
 *   sx,sy   - "current" upper left corner of the mask tile region of interest
 */
void
MTLMaskFill_MaskFill(MTLContext *mtlc,
                     jint x, jint y, jint w, jint h,
                     jint maskoff, jint maskscan, jint masklen,
                     unsigned char *pMask)
{
    //TODO
    J2dTracePrimitive("MTLMaskFill_MaskFill");
    J2dTraceLn(J2D_TRACE_INFO, "MTLMaskFill_MaskFill");
}

JNIEXPORT void JNICALL
Java_sun_java2d_metal_MTLMaskFill_maskFill
    (JNIEnv *env, jobject self,
     jint x, jint y, jint w, jint h,
     jint maskoff, jint maskscan, jint masklen,
     jbyteArray maskArray)
{
    MTLContext *mtlc = MTLRenderQueue_GetCurrentContext();
    unsigned char *mask;
    //TODO
    J2dTracePrimitive("MTLMaskFill_maskFill");
    J2dTraceLn(J2D_TRACE_ERROR, "MTLMaskFill_maskFill");
}

#endif /* !HEADLESS */
