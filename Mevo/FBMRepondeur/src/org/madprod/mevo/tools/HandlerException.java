package org.madprod.mevo.tools;



import org.xmlpull.v1.XmlPullParser; 
import java.io.IOException;

    /**
     * General {@link IOException} that indicates a problem occured while
     * parsing or applying an {@link XmlPullParser}.
     */
    public class HandlerException extends IOException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public HandlerException(String message) {
            super(message);
        }

        public HandlerException(String message, Throwable cause) {
            super(message);
            initCause(cause);
        }

        @Override
        public String toString() {
            if (getCause() != null) {
                return getLocalizedMessage() + ": " + getCause();
            } else {
                return getLocalizedMessage();
            }
        }
    }
