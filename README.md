# Simplr

An app which tailors using Tumblr on a mobile device to personal tastes. While some aesthetic choices were made, the main goal was to create a MVP which creates the desired experience.

Thanks to the team behind [![Jumblr](https://github.com/tumblr/jumblr)], without which this would have not been possible!

# Use Case

This app is intended for personal use, but I've decided to publish it because it's taken a lot of work to even get this far. In my use case, there were initially a couple of features I wanted to implement:
* Reading posts in chronological order
* Filtering out posts in ways that aren't supported in tags (reblogs and posts without images)
* Easily fall back on Tumblr for functionality that wasn't as important to me (even "basic" features that such as video posts and messaging)

Of course I want it to be an aesthetically pleasing experience, but that isn't as important as getting the MVP.

The current way someone uses Simplr is by downloading both it and Tumblr on the same device. When you have entered the app for the first time, you can pull the stream settings from the options menu and pick out which blogs are filtered and how. Then you exit the app, because Simplr acquires and filters posts only while you're outside of the app. When you check Simplr again posts will be shown one at a time, only progressing when you press next on the top. If you want to reblog or send a post, clicking on the author's name on the top will open the post in Tumblr itself (giving you access to the full functionality). This can also be done to actually see video posts or other types that are unsupported. When you're up to date you can exit the app, focusing on whatever else you'd like to do while Simplr collects new posts.

# Lessons Learned

Of course a year or even a month from now I'll face palm at some of my choices here, but I've also learned a lot through making this.
* Concurrency issues are fascinating in this context, making sure that I didn't suffer deadlock and figuring out how much of a load my phone could take.
* I had minimal experience with handling fragments before this project. It's the tip of the iceberg but in particular I learned some nuances about initializing UI elements and the fragment itself.
* It was a helpful experience in dogfooding, since in the last month or so I could use the app and then think on how the flow really needed to be changed.

# Future Steps

While there are no plans to improve on this immediately, things I would do include:
* Refining my code flows in general. I'm certain there are better ways I could handle my concurrency for instance.
* Making it aesthetically pleasing. The rounded edges of the posts are a good start, but choosing a richer background color and other small tweeks could be made.
* Showing more for post types that I don't actually handle in the app.

In addition I'm open to constructive criticism of the code - feel free to add issues or contact me if you feel so inclined!