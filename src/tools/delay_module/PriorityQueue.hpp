#ifndef __PRIORITYQUEUE_HPP__
#define __PRIORITYQUEUE_HPP__

#include <click/vector.hh>
#include <click/heap.hh>

CLICK_DECLS

template <typename T, typename Compare = less<T> >
class PriorityQueue { 
public:

	PriorityQueue() : _compare() { }
	PriorityQueue(const Compare &c) : _compare(c) { }

	bool empty() const { return _v.empty(); }
	int size() const { return _v.size(); }
	const T &top() const { return _v.front(); }
	void push(const T &v) {
	    _v.push_back(v);
	    push_heap(_v.begin(), _v.end(), _compare);
	}
	void pop() {
	    pop_heap(_v.begin(), _v.end(), _compare);
	    _v.pop_back();
	}

private:

	Vector<T> _v;
	Compare _compare;
};

CLICK_ENDDECLS
#endif
