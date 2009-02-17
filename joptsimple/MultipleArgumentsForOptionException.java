/*
 Copyright (c) 2004-2009 Paul R. Holser, Jr.

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package joptsimple;

import java.util.Collection;

/**
 * <p>Thrown when asking an {@link OptionSet} for a single argument of an option when
 * many have been specified.</p>
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: MultipleArgumentsForOptionException.java,v 1.10 2008/12/17 04:10:57 pholser Exp $
 */
class MultipleArgumentsForOptionException extends OptionException {
    private static final long serialVersionUID = -1L;

    MultipleArgumentsForOptionException( Collection<String> options ) {
        super( options );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return "Found multiple arguments for option " + multipleOptionMessage()
            + ", but you asked for only one";
    }
}
