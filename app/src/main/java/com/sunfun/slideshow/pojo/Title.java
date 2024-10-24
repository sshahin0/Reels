package com.sunfun.slideshow.pojo;


    public class Title
    {
        private String size;

        private String text;

        public String getSize ()
        {
            return size;
        }

        public void setSize (String size)
        {
            this.size = size;
        }

        public String getText ()
        {
            return text;
        }

        public void setText (String text)
        {
            this.text = text;
        }

        @Override
        public String toString()
        {
            return "ClassPojo [size = "+size+", text = "+text+"]";
        }



}
