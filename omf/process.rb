#!/usr/bin/ruby

require 'csv'

def consolidateCSVs(infile, outfile) 

	files_ins_csv = `find . -iname "#{infile}" | sort -r`.split

	delays = Array.new(101)
	delays[0]=Array.new(files_ins_csv.length+1)
	delays[0][0]='CDF'

	file_idx = 1


	files_ins_csv.each { | filename | 
		puts "#{filename}"
		file_parts = filename.split('/')
		delay = 0
		file_parts.each { | part | 
			if /^.*_[0-9]{4}$/.match(part) then
				delay = part.split('d_')[1].to_i
			end
		}
		delays[0][file_idx] = "#{delay} {/Symbol m}s"
		
		index = 1

		# Get the contents
		contents = File.open(filename).each_line { | file_line |
			# Get the CDF index and value
			a,b = file_line.chomp.split(',')
			if delays[index].nil? then
				delays[index] = Array.new(files_ins_csv.length)
				delays[index][0]=b
			end
			delays[index][file_idx]=a.to_i/1000.0
			index = index+1
		}

		file_idx = file_idx + 1
	}

	CSV.open("#{outfile}", 'w') { |f|
		delays.each { |row|
			f << row
		}
	}
end

consolidateCSVs("clt-ins-rtt.csv","clt-ins-all.csv")
consolidateCSVs("clt-lkp-rtt.csv","clt-lkp-all.csv")

